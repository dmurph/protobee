package edu.cornell.jnutella.network;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.identity.NetworkIdentityManager;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.ProtocolConfig;

/**
 * Multiplexer for receiving arbitrary network connections. After matching a protocol, this class
 * will get or create a network identity, associate the remote connection with the chosen protocol
 * for that identity, and create a new session. If the identity is there and there is a current
 * session, then it will log the error and close the connection.
 * 
 * This class will be a singleton, so it must be threadsafe.
 * 
 * @author Daniel
 */
public class ReceivingRequestMultiplexer extends FrameDecoderLE {

  @InjectLogger
  private Logger logger;
  private final Object loggerLock = new Object();

  private final NetworkIdentityManager identityManager;

  private final Map<Protocol, ProtocolConfig> protocols;
  private final Object providersLock = new Object();

  @Inject
  public ReceivingRequestMultiplexer(Set<ProtocolConfig> protocolConfigs,
      NetworkIdentityManager identityManager) {
    this.identityManager = identityManager;

    ImmutableMap.Builder<Protocol, ProtocolConfig> builder = ImmutableMap.builder();

    for (ProtocolConfig config : protocolConfigs) {
      Protocol protocolDef = config.getClass().getAnnotation(Protocol.class);
      if (protocolDef == null) {
        logger.error("Protocol config '" + config + "' does not have Protocol annotation");
        throw new IllegalArgumentException("Protocol config missing Protocol annotation");
      }
      builder.put(protocolDef, config);
    }
    protocols = builder.build();
  }

  @Override
  protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer)
      throws Exception {

    String data = buffer.toString(Charset.forName("UTF-8"));
    // update the reader index so netty doesn't yell at us
    buffer.readerIndex(buffer.readableBytes());
    if (!data.contains("\r\n")) {
      buffer.resetReaderIndex();
      return null;
    }
    String header = data.substring(0, data.indexOf("\r\n"));

    boolean found = false;
    // iteration should be threadsafe, because we're immutable
    for (Protocol key : protocols.keySet()) {
      if (header.matches(key.headerRegex())) {
        found = true;
        initializeChannel(ctx, channel, key, protocols.get(key));
        break;
      }
    }

    if (!found) {
      synchronized (loggerLock) {
        logger.error("Could not find protocol for header '" + header + "', closing channel.");
      }
      ctx.getChannel().close();
      return null;
    }

    ChannelBuffer newBuffer = newCumulationBuffer(ctx, buffer.readableBytes());
    newBuffer.writeBytes(buffer);
    return newBuffer;
  }

  private void initializeChannel(ChannelHandlerContext ctx, Channel channel, Protocol protocol,
      ProtocolConfig protocolProvider) {
    Iterable<ChannelHandler> handlers;

    synchronized (providersLock) {
      handlers = protocolProvider.createChannelHandlers();
      NetworkIdentity identity;
      if (identityManager.hasNetworkIdentity(channel.getRemoteAddress())) {
        identity = identityManager.getNewtorkIdentity(channel.getRemoteAddress());
        if (identity.hasCurrentSession(protocol)) {
          logger.error("Protocol " + protocol + " already has a session running for identity "
              + identity + ", closing channel.");
          channel.close();
          return;
        }
      } else {
        identity = identityManager.createNetworkIdentity();
      }
      identityManager.setNetworkAddress(identity, protocol, channel.getRemoteAddress());
      identity.createNewSession(channel, protocol);
    }

    ChannelPipeline pipeline = ctx.getPipeline();
    for (ChannelHandler channelHandler : handlers) {
      pipeline.addLast(channelHandler.toString(), channelHandler);
    }
    pipeline.remove(this);
  }
}

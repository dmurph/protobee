package edu.cornell.jnutella.network;

import java.nio.charset.Charset;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.slf4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.identity.NetworkIdentityManager;
import edu.cornell.jnutella.identity.ProtocolIdentityModel;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.ProtocolConfig;

/**
 * Multiplexer for receiving arbitrary network connections. After matching a protocol, this class
 * will create the session handshake handlers using the {@link HandshakeStateBootstrapper}, which
 * then creates the handlers from the protocol config after the handshake is complete
 * 
 * This class will be a singleton, so it must be threadsafe.
 * 
 * @author Daniel
 */
@Singleton
public class ReceivingRequestMultiplexer extends FrameDecoderLE {

  @InjectLogger
  private Logger log;
  private final Object loggerLock = new Object();

  private final NetworkIdentityManager identityManager;
  private final HandshakeStateBootstrapper handshakeBootstrap;
  private final Object bootstrapLock = new Object();

  private final Map<Protocol, ProtocolConfig> protocols;

  @Inject
  public ReceivingRequestMultiplexer(Map<Protocol, ProtocolConfig> protocols,
      HandshakeStateBootstrapper handshakeBootstrap, NetworkIdentityManager identityManager) {
    this.protocols = protocols;
    this.handshakeBootstrap = handshakeBootstrap;
    this.identityManager = identityManager;
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
        log.error("Could not find protocol for header '" + header + "', closing channel.");
      }
      channel.close();
      return null;
    }

    ChannelBuffer newBuffer = newCumulationBuffer(ctx, buffer.readableBytes());
    newBuffer.writeBytes(buffer);
    return newBuffer;
  }

  private void initializeChannel(ChannelHandlerContext ctx, Channel channel, Protocol protocol,
      ProtocolConfig protocolProvider) {
    ChannelHandler[] handshakeHandlers;
    synchronized (bootstrapLock) {
      NetworkIdentity identity =
          identityManager.getNetworkIdentityWithNewConnection(protocol, channel.getRemoteAddress());
      identity.enterScope();
      ProtocolIdentityModel identityModel = identity.getModel(protocol);
      handshakeHandlers =
          handshakeBootstrap.bootstrapSession(protocolProvider, identityModel,
              channel.getRemoteAddress(), channel);
      identity.exitScope();
    }

    ChannelPipeline pipeline = ctx.getPipeline();
    for (ChannelHandler channelHandler : handshakeHandlers) {
      pipeline.addLast(channelHandler.toString(), channelHandler);
    }
    // TODO: will we have another handler?
    pipeline.remove(this);
  }
}

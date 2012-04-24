package edu.cornell.jnutella.network;

import java.nio.charset.Charset;
import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.slf4j.Logger;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.identity.NetworkIdentityManager;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.ProtocolConfig;

/**
 * Multiplexer for receiving arbitrary network connections. After matching a protocol, this class
 * will create the session handshake handlers using the {@link HandshakeStateBootstrapper}, which
 * then creates the handlers from the protocol config after the handshake is complete
 * 
 * @author Daniel
 */
public class ReceivingRequestMultiplexer extends FrameDecoderLE {

  public static interface Factory {
    ReceivingRequestMultiplexer create(Set<ProtocolConfig> protocolsMultiplexing);
  }

  @InjectLogger
  private Logger log;

  private final NetworkIdentityManager identityManager;
  private final HandshakeStateBootstrapper handshakeBootstrap;

  private final Set<ProtocolConfig> protocols;

  @AssistedInject
  public ReceivingRequestMultiplexer(@Assisted Set<ProtocolConfig> protocols,
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
    for (ProtocolConfig protocol : protocols) {
      if (header.matches(protocol.get().headerRegex())) {
        found = true;
        initializeChannel(ctx, channel, protocol.get(), protocol);
        break;
      }
    }

    if (!found) {
      log.error("Could not find protocol for header '" + header + "', closing channel.");
      channel.close();
      return null;
    }

    ChannelBuffer newBuffer = newCumulationBuffer(ctx, buffer.readableBytes());
    newBuffer.writeBytes(buffer);
    return newBuffer;
  }

  private void initializeChannel(ChannelHandlerContext ctx, Channel channel, Protocol protocol,
      ProtocolConfig protocolProvider) {
    NetworkIdentity identity =
        identityManager.getNetworkIdentityWithNewConnection(protocol, channel.getRemoteAddress());
    ChannelPipeline pipeline = ctx.getPipeline();
    handshakeBootstrap.bootstrapSession(protocolProvider, identity, channel.getRemoteAddress(),
        channel, pipeline);

    // this should be the only handler we added to this pipeline
    pipeline.remove(this);
  }
}

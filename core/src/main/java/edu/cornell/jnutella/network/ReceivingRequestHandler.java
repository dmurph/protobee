package edu.cornell.jnutella.network;

import java.nio.charset.Charset;

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
 * Sets up a protocol session when a request is received.
 * 
 * @author Daniel
 */
public class ReceivingRequestHandler extends FrameDecoderLE {

  public static interface Factory {
    ReceivingRequestHandler create(ProtocolConfig protocol);
  }

  @InjectLogger
  private Logger log;

  private final NetworkIdentityManager identityManager;
  private final HandshakeStateBootstrapper handshakeBootstrap;
  private final ProtocolConfig protocolConfig;

  @AssistedInject
  public ReceivingRequestHandler(@Assisted ProtocolConfig protocol,
      HandshakeStateBootstrapper handshakeBootstrap, NetworkIdentityManager identityManager) {
    this.protocolConfig = protocol;
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
    Protocol protocol = protocolConfig.get();
    if (!header.matches(protocol.headerRegex())) {
      log.error("Request header '" + header + "' doesn't match " + protocolConfig
          + " protocol header, closing channel.");
      channel.close();
      return null;
    }

    ChannelPipeline pipeline = ctx.getPipeline();

    NetworkIdentity identity =
        identityManager.getNetworkIdentityWithNewConnection(protocol, channel.getRemoteAddress());
    handshakeBootstrap.bootstrapSession(protocolConfig, identity, channel.getRemoteAddress(),
        channel, pipeline);

    // this should be the only handler we added to this pipeline
    pipeline.remove(this);

    ChannelBuffer newBuffer = newCumulationBuffer(ctx, buffer.readableBytes());
    newBuffer.writeBytes(buffer);
    return newBuffer;
  }
}

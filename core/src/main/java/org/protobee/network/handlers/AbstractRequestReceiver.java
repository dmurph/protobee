package org.protobee.network.handlers;

import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.protobee.identity.NetworkIdentity;
import org.protobee.identity.NetworkIdentityManager;
import org.protobee.network.ProtobeeChannels;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolConfig;
import org.protobee.session.handshake.HandshakeStateBootstrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractRequestReceiver extends FrameDecoderLE {

  private static final Logger log = LoggerFactory.getLogger(AbstractRequestReceiver.class);

  private final NetworkIdentityManager identityManager;
  private final HandshakeStateBootstrapper handshakeBootstrap;
  private final ProtobeeChannels channels;

  public AbstractRequestReceiver(HandshakeStateBootstrapper handshakeBootstrap,
      NetworkIdentityManager identityManager, ProtobeeChannels channels) {
    this.handshakeBootstrap = handshakeBootstrap;
    this.identityManager = identityManager;
    this.channels = channels;
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
    ProtocolConfig protocolConfig = getMatchingConfig(header);
    if (protocolConfig == null) {
      log.error("Request header '" + header + "' doesn't match a protocol header, closing channel.");
      channel.close();
      return null;
    }

    Protocol protocol = protocolConfig.get();

    // add our newly connected channel to the group
    channels.addChannel(channel, protocol);

    ChannelPipeline pipeline = ctx.getPipeline();

    NetworkIdentity identity =
        identityManager.getNetworkIdentityWithNewConnection(protocol, channel.getRemoteAddress());
    handshakeBootstrap.bootstrapSession(protocolConfig, identity, channel, pipeline);

    // this should be the only handler we added to this pipeline
    pipeline.remove(this);

    ChannelBuffer newBuffer = newCumulationBuffer(ctx, buffer.readableBytes());
    newBuffer.writeBytes(buffer);
    return newBuffer;
  }

  protected abstract ProtocolConfig getMatchingConfig(String header);
}

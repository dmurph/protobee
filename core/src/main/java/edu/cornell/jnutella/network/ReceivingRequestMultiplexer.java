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

import com.google.common.collect.Maps;
import com.google.inject.Inject;

import edu.cornell.jnutella.ConnectionManager;
import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.session.SessionModel;

public class ReceivingRequestMultiplexer extends FrameDecoderLE {

  @InjectLogger
  private Logger logger;
  private final ConnectionManager connectionManager;
  private final Map<String, ProtocolConfigProvider> protocolProviders = Maps.newHashMap();

  @Inject
  public ReceivingRequestMultiplexer(ConnectionManager connectionMananger,
      Set<ProtocolConfigProvider> channelHandlerProviders) {
    this.connectionManager = connectionMananger;

    for (ProtocolConfigProvider provider : channelHandlerProviders) {
      protocolProviders.put(provider.getProtocol().getHeaderRegex(), provider);
    }
  }

  @Override
  protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer)
      throws Exception {

    String data = buffer.toString(Charset.forName("UTF-8"));
    // update the reader index so netty doesn't yell at us
    buffer.readerIndex(buffer.readableBytes());
    if (!data.contains("\r\n")) {
      return null;
    }
    String header = data.substring(0, data.indexOf("\r\n"));

    boolean found = false;
    for (String key : protocolProviders.keySet()) {
      if (header.matches(key)) {
        found = true;
        initializeChannel(ctx, channel, protocolProviders.get(key));
        break;
      }
    }

    if (!found) {
      logger.error("Could not find protocol for header '" + header + "', closing channel.");
      ctx.getChannel().close();
      return null;
    }

    ChannelBuffer newBuffer = newCumulationBuffer(ctx, buffer.readableBytes());
    newBuffer.writeBytes(buffer);
    return newBuffer;
  }

  private void initializeChannel(ChannelHandlerContext ctx, Channel channel,
      ProtocolConfigProvider protocolProvider) {
    ChannelHandler[] handlers = protocolProvider.createChannelHandlers();
    SessionModel model = protocolProvider.createSessionModel();

    ChannelPipeline pipeline = ctx.getPipeline();
    for (ChannelHandler channelHandler : handlers) {
      pipeline.addLast(channelHandler.toString(), channelHandler);
    }
    pipeline.remove(this);

    connectionManager.addSessionModel(channel.getRemoteAddress(), model);
  }
}

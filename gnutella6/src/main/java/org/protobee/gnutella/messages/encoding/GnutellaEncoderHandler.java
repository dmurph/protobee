package org.protobee.gnutella.messages.encoding;

import java.nio.ByteOrder;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelDownstreamHandler;
import org.protobee.gnutella.messages.GnutellaMessage;
import org.protobee.gnutella.messages.MessageHeader;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;


public class GnutellaEncoderHandler extends SimpleChannelDownstreamHandler {

  private final GnutellaMessageEncoder messageEncoder;

  @Inject
  public GnutellaEncoderHandler(GnutellaMessageEncoder messageEncoder) {
    this.messageEncoder = messageEncoder;
  }

  @Override
  public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    Preconditions.checkArgument(e.getMessage() instanceof GnutellaMessage,
        "Did not get a gnutella message");
    GnutellaMessage message = (GnutellaMessage) e.getMessage();

    ChannelBuffer messageBuffer = ChannelBuffers.dynamicBuffer(ByteOrder.LITTLE_ENDIAN, MessageHeader.MESSAGE_HEADER_LENGTH
            + 200);
    // TODO add estimate length method to body
    
    messageEncoder.encode(messageBuffer, message);
    
    Channels.write(ctx, e.getFuture(), messageBuffer);
  }
}

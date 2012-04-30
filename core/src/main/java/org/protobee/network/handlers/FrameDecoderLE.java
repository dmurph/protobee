package org.protobee.network.handlers;

import java.nio.ByteOrder;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferFactory;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

public abstract class FrameDecoderLE extends FrameDecoder {

  public FrameDecoderLE() {
    super();
  }

  public FrameDecoderLE(boolean unfold) {
    super(unfold);
  }

  @Override
  protected ChannelBuffer newCumulationBuffer(ChannelHandlerContext ctx, int minimumCapacity) {
    ChannelBufferFactory factory = ctx.getChannel().getConfig().getBufferFactory();
    return ChannelBuffers.dynamicBuffer(ByteOrder.LITTLE_ENDIAN, Math.max(minimumCapacity, 256),
        factory);
  }

}

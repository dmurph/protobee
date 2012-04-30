package org.protobee.network;

import java.nio.ByteOrder;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferFactory;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.replay.ReplayingDecoder;

public abstract class ReplayingDecoderLE<T extends Enum<T>> extends ReplayingDecoder<T> {

  public ReplayingDecoderLE() {
    super();
  }

  public ReplayingDecoderLE(boolean unfold) {
    super(unfold);
  }

  public ReplayingDecoderLE(T initialState, boolean unfold) {
    super(initialState, unfold);
  }

  public ReplayingDecoderLE(T initialState) {
    super(initialState);
  }

  @Override
  protected ChannelBuffer newCumulationBuffer(ChannelHandlerContext ctx, int minimumCapacity) {
    ChannelBufferFactory factory = ctx.getChannel().getConfig().getBufferFactory();
    return ChannelBuffers.dynamicBuffer(ByteOrder.LITTLE_ENDIAN, Math.max(minimumCapacity, 256),
        factory);
  }
}

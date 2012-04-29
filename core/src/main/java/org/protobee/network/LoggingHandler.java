package org.protobee.network;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.WriteCompletionEvent;
import org.protobee.annotation.InjectLogger;
import org.slf4j.Logger;


public class LoggingHandler extends SimpleChannelUpstreamHandler {

  @InjectLogger
  private Logger log;

  @Override
  public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    super.channelDisconnected(ctx, e);
    log.info("Channel disconnected: " + e);
  }

  @Override
  public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    super.channelClosed(ctx, e);
    log.info("Channel closed: " + e);
  }

  public void channelBound(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    super.channelBound(ctx, e);
    log.debug("Channel bound: " + e);
  }

  @Override
  public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    super.channelConnected(ctx, e);
    log.debug("Channel connected: " + e);
  }

  public void channelInterestChanged(ChannelHandlerContext ctx, ChannelStateEvent e)
      throws Exception {
    super.channelInterestChanged(ctx, e);
    log.debug("Channel intereste changed: " + e);
  }

  public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    super.channelOpen(ctx, e);
    log.debug("Channel open: " + e);
  }

  public void channelUnbound(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    super.channelUnbound(ctx, e);
    log.debug("Channel unbound: " + e);
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    super.messageReceived(ctx, e);
    log.debug("Channel message received: " + e);
  }

  @Override
  public void writeComplete(ChannelHandlerContext ctx, WriteCompletionEvent e) throws Exception {
    super.writeComplete(ctx, e);
    log.debug("Channel write complete: " + e);
  }
}

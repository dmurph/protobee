package org.protobee.network.handlers;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.protobee.annotation.InjectLogger;
import org.slf4j.Logger;

import com.google.inject.Singleton;

/**
 * Closes the channel on any exception
 * 
 * @author Daniel
 */
@Singleton
public class CloseOnExceptionHandler extends SimpleChannelUpstreamHandler {

  @InjectLogger
  private Logger log;

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
    log.error("Exception caught, disconnecting", e.getCause());
    e.getChannel().close();
    super.exceptionCaught(ctx, e);
  }
}

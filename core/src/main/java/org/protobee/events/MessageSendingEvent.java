package org.protobee.events;

import org.jboss.netty.channel.ChannelHandlerContext;

public class MessageSendingEvent<T> extends SessionEvent {

  private final T message;
  private final Class<T> mesageClass;

  public MessageSendingEvent(ChannelHandlerContext context, T message, Class<T> messageClass) {
    super(context);
    this.message = message;
    this.mesageClass = messageClass;
  }

  public T getMessage() {
    return message;
  }

  public Class<T> getMesageClass() {
    return mesageClass;
  }
}

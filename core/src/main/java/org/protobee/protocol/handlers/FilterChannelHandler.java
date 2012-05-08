package org.protobee.protocol.handlers;

import java.util.Set;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.protobee.protocol.Protocol;
import org.protobee.stats.DropLog;
import org.protobee.util.PreFilter;

import com.google.common.base.Preconditions;

/**
 * Filter handler for filtering out messages, given a set of filters. The drop log is called when a
 * message is filtered, with the reason/s from the filters.
 * 
 * @author Daniel
 * @param <T> the message type for the filters
 */
public class FilterChannelHandler<T> extends SimpleChannelUpstreamHandler {

  private final Set<PreFilter<T>> filters;
  private final DropLog dropLog;
  private final Protocol protocol;
  private final FilterMode filterMode;
  private final Class<T> filterClass;

  public FilterChannelHandler(Set<PreFilter<T>> filters, DropLog dropLog, Protocol protocol,
      Class<T> messageClass, FilterMode mode) {
    Preconditions.checkNotNull(filters);
    Preconditions.checkNotNull(dropLog);
    Preconditions.checkNotNull(protocol);
    Preconditions.checkNotNull(messageClass);
    Preconditions.checkNotNull(mode);
    this.filters = filters;
    this.dropLog = dropLog;
    this.protocol = protocol;
    this.filterMode = mode;
    this.filterClass = messageClass;
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    Object objectMessage = e.getMessage();

    T message = null;
    if (filterClass.isAssignableFrom(objectMessage.getClass())) {
      message = filterClass.cast(objectMessage);
    }

    Preconditions.checkState(message != null || filterMode == FilterMode.SKIP_MISMATCHED_TYPES,
        "Type mismatch, cannot convert from " + objectMessage.getClass() + " to " + filterClass);

    if (message == null) {
      super.messageReceived(ctx, e);
      return;
    }

    StringBuilder reasons = null;
    for (PreFilter<T> filter : filters) {
      String reason = filter.shouldFilter(message);
      if (reason != null) {
        if (reasons == null) {
          reasons = new StringBuilder();
          reasons.append("[").append(reason);
        } else {
          reasons.append(", ").append(reason);
        }
      }
    }
    if (reasons != null) {
      reasons.append("]");
      dropLog.messageDropped(e.getRemoteAddress(), protocol, message, reasons.toString());
    } else {
      super.messageReceived(ctx, e);
    }
  }
}

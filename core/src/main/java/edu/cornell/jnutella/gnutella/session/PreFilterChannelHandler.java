package edu.cornell.jnutella.gnutella.session;

import java.util.Set;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import edu.cornell.jnutella.gnutella.Gnutella;
import edu.cornell.jnutella.gnutella.filters.GnutellaPreFilter;
import edu.cornell.jnutella.gnutella.messages.GnutellaMessage;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.stats.DropLog;

/**
 * Channel handler for prefiltering the gnutella messages before they reach the modules
 * 
 * @author Daniel
 */
public class PreFilterChannelHandler extends SimpleChannelUpstreamHandler {

  private final Set<GnutellaPreFilter> prefilters;
  private final DropLog dropLog;
  private final Protocol gnutella;

  @Inject
  public PreFilterChannelHandler(Set<GnutellaPreFilter> prefilters, DropLog dropLog,
      @Gnutella Protocol gnutella) {
    this.prefilters = prefilters;
    this.dropLog = dropLog;
    this.gnutella = gnutella;
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    Preconditions
        .checkArgument(e.getMessage() instanceof GnutellaMessage, "Not a gnutella message");
    GnutellaMessage message = (GnutellaMessage) e.getMessage();

    StringBuilder reasons = null;
    for (GnutellaPreFilter filter : prefilters) {
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
      dropLog.messageDropped(e.getRemoteAddress(), gnutella, message, reasons.toString());
    } else {
      super.messageReceived(ctx, e);
    }
  }
}

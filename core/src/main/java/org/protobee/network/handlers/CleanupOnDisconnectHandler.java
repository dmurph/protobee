package org.protobee.network.handlers;

import java.net.SocketAddress;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.protobee.annotation.InjectLogger;
import org.protobee.guice.SessionScope;
import org.protobee.identity.NetworkIdentity;
import org.protobee.protocol.Protocol;
import org.protobee.session.SessionModel;
import org.protobee.stats.DropLog;
import org.slf4j.Logger;

import com.google.inject.Inject;


/**
 * Closes the channel on disconnect and cleans up any current sessions. Calls the {@link DropLog}
 * when a channel is disconnected
 * 
 * @author Daniel
 */
@SessionScope
public class CleanupOnDisconnectHandler extends SimpleChannelUpstreamHandler {

  @InjectLogger
  private Logger log;
  private final NetworkIdentity identity;
  private final SessionModel session;
  private final Protocol protocol;
  private final DropLog dropLog;

  @Inject
  public CleanupOnDisconnectHandler(SessionModel session, NetworkIdentity identity,
      Protocol protocol, DropLog dropLog) {
    this.identity = identity;
    this.session = session;
    this.protocol = protocol;
    this.dropLog = dropLog;
  }

  @Override
  public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    SocketAddress remoteAddress = e.getChannel().getRemoteAddress();
    if (identity.hasCurrentSession(protocol)) {
      if (identity.getCurrentSession(protocol) == session) {
        log.info("Clearing current session for connection " + remoteAddress);
        identity.clearCurrentSession(protocol);
      } else {
        log.warn("Not cleaing session, it doesn't match");
      }
    } else {
      log.debug("Session already cleared for connection " + remoteAddress);
    }
    log.info("Channel disconnecting, closing");
    e.getChannel().close();
    dropLog.connectionDisconnected(remoteAddress, protocol);
    super.channelDisconnected(ctx, e);
  }

  @Override
  public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    if (identity.hasCurrentSession(protocol)) {
      if (identity.getCurrentSession(protocol) == session) {
        log.info("Session not cleared yet on close, clearing " + e.getChannel().getRemoteAddress());
        identity.clearCurrentSession(protocol);
      } else {
        log.warn("New session already");
      }
    } else {
      log.debug("Session already cleared on close for connection "
          + e.getChannel().getRemoteAddress());
    }
    super.channelClosed(ctx, e);
  }
}

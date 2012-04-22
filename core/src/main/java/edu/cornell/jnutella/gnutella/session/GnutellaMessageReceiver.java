package edu.cornell.jnutella.gnutella.session;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.gnutella.messages.GnutellaMessage;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.session.SessionModel;

@Singleton
public class GnutellaMessageReceiver extends SimpleChannelUpstreamHandler {

  @InjectLogger
  private Logger log;
  private final Provider<EventBus> eventBus;
  private final Provider<NetworkIdentity> identity;
  private final Provider<SessionModel> session;

  @Inject
  public GnutellaMessageReceiver(Provider<EventBus> eventBus, Provider<NetworkIdentity> identity,
      Provider<SessionModel> session) {
    this.eventBus = eventBus;
    this.identity = identity;
    this.session = session;
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    Preconditions.checkArgument(e.getMessage() instanceof GnutellaMessage,
        "Given message not a gnutella message");

    GnutellaMessage message = (GnutellaMessage) e.getMessage();
    log.debug("Got message: " + message);

    NetworkIdentity currIdentity = identity.get();
    SessionModel currSession = session.get();
    currIdentity.enterScope();
    currSession.enterScope();

    eventBus.get().post(new MessageReceivedEvent(ctx, message));

    e.getFuture().setSuccess();
    currSession.exitScope();
    currIdentity.exitScope();
  }
}

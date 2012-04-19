package edu.cornell.jnutella.gnutella.session;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelDownstreamHandler;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.gnutella.messages.GnutellaMessage;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.protocol.session.SessionModel;

@Singleton
public class GnutellaMessageWriter extends SimpleChannelDownstreamHandler {

  @InjectLogger
  private Logger log;
  private final Provider<EventBus> eventBus;
  private final Provider<NetworkIdentity> identity;
  private final Provider<SessionModel> session;

  @Inject
  public GnutellaMessageWriter(Provider<EventBus> eventBus, Provider<NetworkIdentity> identity,
      Provider<SessionModel> session) {
    this.eventBus = eventBus;
    this.identity = identity;
    this.session = session;
  }

  @Override
  public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    Preconditions.checkArgument(e.getMessage() instanceof GnutellaMessage,
        "Given message not a gnutella message");

    GnutellaMessage message = (GnutellaMessage) e.getMessage();

    NetworkIdentity currIdentity = identity.get();
    SessionModel currSession = session.get();
    currIdentity.enterScope();
    currSession.enterScope();
    
    eventBus.get().post(new MessageSendingEvent(ctx, message));
    log.debug("Writing message: " + message);
    
    currSession.exitScope();
    currIdentity.exitScope();
    
    Channels.write(ctx, e.getFuture(), message);
  }
}

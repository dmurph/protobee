package edu.cornell.jnutella.gnutella.modules;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import edu.cornell.jnutella.gnutella.Gnutella;
import edu.cornell.jnutella.gnutella.GnutellaIdentityModel;
import edu.cornell.jnutella.gnutella.RequestFilter;
import edu.cornell.jnutella.gnutella.messages.GnutellaMessage;
import edu.cornell.jnutella.gnutella.messages.MessageHeader;
import edu.cornell.jnutella.gnutella.messages.PushBody;
import edu.cornell.jnutella.gnutella.routing.InvalidMessageException;
import edu.cornell.jnutella.gnutella.routing.managers.PushRoutingTableManager;
import edu.cornell.jnutella.gnutella.session.MessageDispatcher;
import edu.cornell.jnutella.gnutella.session.MessageReceivedEvent;
import edu.cornell.jnutella.guice.SessionScope;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.modules.ProtocolModule;
import edu.cornell.jnutella.protocol.Protocol;

@SessionScope
public class PushModule implements ProtocolModule {

  private final RequestFilter filter;
  private final PushRoutingTableManager pushRTManager;
  private final MessageDispatcher messageDispatcher;
  private final NetworkIdentity identity;
  private final Protocol gnutella;
  private final MessageHeader.Factory headerFactory;

  @Inject
  public PushModule(RequestFilter filter, MessageHeader.Factory headerFactory,
                    PushRoutingTableManager pushRTManager,
                    @Gnutella Protocol gnutella, MessageDispatcher messageDispatcher,
                    NetworkIdentity identity) {
    this.filter = filter;
    this.pushRTManager = pushRTManager;
    this.gnutella = gnutella;
    this.messageDispatcher = messageDispatcher;
    this.identity = identity;
    this.headerFactory = headerFactory;
  }

  // map message header to an null when originally sending query
  private void pushMessageRecieved(MessageReceivedEvent event, MessageHeader header) throws InvalidMessageException {

    PushBody pushBody =  (PushBody) event.getMessage().getBody();
    GnutellaIdentityModel identityModel = (GnutellaIdentityModel) identity.getModel(gnutella);

    if (pushBody.getServantID().equals(identityModel.getGuid())){
      // use locally
    }

    if (!filter.shouldRoutePushMessage(header.getTtl(), identityModel.getGuid())){ return; }

    MessageHeader newHeader = headerFactory.create(header.getGuid(), MessageHeader.F_PUSH, (byte) (header.getTtl() - 1), (byte) (header.getHops() + 1));
    
    // route push message
    messageDispatcher.dispatchMessage(pushRTManager.findRouting(identityModel.getGuid()), new GnutellaMessage(newHeader, event.getMessage().getBody()));
  }

  @Subscribe
  public void messageReceived(MessageReceivedEvent event) throws InvalidMessageException {
    MessageHeader header = event.getMessage().getHeader();
    if (header.getPayloadType() == MessageHeader.F_PUSH) {
      pushMessageRecieved(event, header);
    }
  }
}

package edu.cornell.jnutella.gnutella.modules;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import edu.cornell.jnutella.gnutella.RequestFilter;
import edu.cornell.jnutella.gnutella.messages.MessageHeader;
import edu.cornell.jnutella.gnutella.routing.InvalidMessageException;
import edu.cornell.jnutella.gnutella.routing.managers.CoreRoutingTableManager;
import edu.cornell.jnutella.gnutella.routing.message.RoutingBody;
import edu.cornell.jnutella.gnutella.session.MessageReceivedEvent;
import edu.cornell.jnutella.guice.SessionScope;
import edu.cornell.jnutella.modules.ProtocolModule;

@SessionScope
public class RoutingModule implements ProtocolModule {

  private final RequestFilter filter;
  private final CoreRoutingTableManager coreRTManager;

  @Inject
  public RoutingModule(RequestFilter filter, CoreRoutingTableManager coreRTManager) {
    this.filter = filter;
    this.coreRTManager = coreRTManager;
  }

  private void routingMessageRecieved(MessageReceivedEvent event, MessageHeader header) throws InvalidMessageException {
    if (!filter.shouldAcceptRoutingMessage(event.getMessage())) { return; } // check for local ultrapeer
    RoutingBody routingBody =  (RoutingBody) event.getMessage().getBody();
    coreRTManager.update(routingBody);
  }

  @Subscribe
  public void messageReceived(MessageReceivedEvent event) throws InvalidMessageException {
    MessageHeader header = event.getMessage().getHeader();
    if (header.getPayloadType() == MessageHeader.F_ROUTE_TABLE_UPDATE) {
      routingMessageRecieved(event, header);
    }
  }
}

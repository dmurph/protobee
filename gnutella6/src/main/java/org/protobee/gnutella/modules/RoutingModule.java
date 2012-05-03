package org.protobee.gnutella.modules;

import org.protobee.compatability.Headers;
import org.protobee.gnutella.RequestFilter;
import org.protobee.gnutella.messages.MessageHeader;
import org.protobee.gnutella.routing.InvalidMessageException;
import org.protobee.gnutella.routing.managers.CoreRoutingTableManager;
import org.protobee.gnutella.routing.message.RoutingBody;
import org.protobee.gnutella.session.MessageReceivedEvent;
import org.protobee.guice.scopes.SessionScope;
import org.protobee.modules.ProtocolModule;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

@Headers(required = {})
@SessionScope
public class RoutingModule extends ProtocolModule {

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

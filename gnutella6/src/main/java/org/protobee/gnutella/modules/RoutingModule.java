package org.protobee.gnutella.modules;

import org.protobee.gnutella.messages.MessageHeader;
import org.protobee.gnutella.routing.InvalidMessageException;
import org.protobee.gnutella.routing.managers.CoreRoutingTableManager;
import org.protobee.gnutella.routing.message.RoutingBody;
import org.protobee.gnutella.session.MessageReceivedEvent;
import org.protobee.guice.SessionScope;
import org.protobee.modules.ProtocolModule;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;


@SessionScope
public class RoutingModule implements ProtocolModule {

  private final CoreRoutingTableManager coreRTManager;

  @Inject
  public RoutingModule(CoreRoutingTableManager coreRTManager) {
    this.coreRTManager = coreRTManager;
  }

  private void routingMessageRecieved(MessageReceivedEvent event, MessageHeader header) throws InvalidMessageException {
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

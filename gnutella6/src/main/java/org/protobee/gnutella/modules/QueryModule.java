package org.protobee.gnutella.modules;

import org.protobee.gnutella.messages.MessageHeader;
import org.protobee.gnutella.routing.InvalidMessageException;
import org.protobee.gnutella.routing.managers.QueryRoutingTableManager;
import org.protobee.gnutella.session.MessageReceivedEvent;
import org.protobee.guice.SessionScope;
import org.protobee.identity.NetworkIdentity;
import org.protobee.modules.ProtocolModule;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;


@SessionScope
public class QueryModule implements ProtocolModule {

  private final NetworkIdentity identity;
  private final QueryRoutingTableManager queryRTManager;

  @Inject
  public QueryModule(NetworkIdentity identity, QueryRoutingTableManager queryRTManager) {
    this.queryRTManager = queryRTManager;
    this.identity = identity;
  }

  private void queryMessageRecieved(MessageReceivedEvent event, MessageHeader header) throws InvalidMessageException {

    queryRTManager.addRouting(header.getGuid(), identity);

    // respond to query

  }

  @Subscribe
  public void messageReceived(MessageReceivedEvent event) throws InvalidMessageException {
    MessageHeader header = event.getMessage().getHeader();
    if (header.getPayloadType() == MessageHeader.F_QUERY) {
      queryMessageRecieved(event, header);
    }
  }
}

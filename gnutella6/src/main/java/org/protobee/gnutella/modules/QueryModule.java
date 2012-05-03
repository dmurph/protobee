package org.protobee.gnutella.modules;

import org.protobee.compatability.Headers;
import org.protobee.gnutella.RequestFilter;
import org.protobee.gnutella.messages.MessageHeader;
import org.protobee.gnutella.messages.QueryBody;
import org.protobee.gnutella.routing.InvalidMessageException;
import org.protobee.gnutella.routing.managers.QueryRoutingTableManager;
import org.protobee.gnutella.session.MessageReceivedEvent;
import org.protobee.guice.scopes.SessionScope;
import org.protobee.identity.NetworkIdentity;
import org.protobee.modules.ProtocolModule;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

@Headers(required = {})
@SessionScope
public class QueryModule extends ProtocolModule {

  private final RequestFilter filter;
  private final NetworkIdentity identity;
  private final QueryRoutingTableManager queryRTManager;

  @Inject
  public QueryModule(RequestFilter filter, NetworkIdentity identity,
                     QueryRoutingTableManager queryRTManager) {
    this.filter = filter;
    this.queryRTManager = queryRTManager;
    this.identity = identity;
  }

  private void queryMessageRecieved(MessageReceivedEvent event, MessageHeader header) throws InvalidMessageException {

    if (!filter.shouldAcceptQueryMessage( header.getGuid(), header.getHops(), (QueryBody) event.getMessage().getBody())) { return; }

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

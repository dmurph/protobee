package edu.cornell.jnutella.gnutella.modules;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import edu.cornell.jnutella.gnutella.RequestFilter;
import edu.cornell.jnutella.gnutella.messages.MessageHeader;
import edu.cornell.jnutella.gnutella.messages.QueryBody;
import edu.cornell.jnutella.gnutella.routing.InvalidMessageException;
import edu.cornell.jnutella.gnutella.routing.managers.QueryRoutingTableManager;
import edu.cornell.jnutella.gnutella.session.MessageReceivedEvent;
import edu.cornell.jnutella.guice.SessionScope;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.modules.ProtocolModule;

@SessionScope
public class QueryModule implements ProtocolModule {

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

    if (!filter.shouldAcceptQueryMessage( header.getGUID(), header.getHops(), (QueryBody) event.getMessage().getBody())) { return; }

    queryRTManager.addRouting(header.getGUID(), identity);

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

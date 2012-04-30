package org.protobee.gnutella.modules;

import org.protobee.gnutella.GnutellaServantModel;
import org.protobee.gnutella.RequestFilter;
import org.protobee.gnutella.messages.GnutellaMessage;
import org.protobee.gnutella.messages.MessageHeader;
import org.protobee.gnutella.messages.QueryHitBody;
import org.protobee.gnutella.routing.IdentityHash;
import org.protobee.gnutella.routing.InvalidMessageException;
import org.protobee.gnutella.routing.QueryGUIDRoutingPair;
import org.protobee.gnutella.routing.managers.PushRoutingTableManager;
import org.protobee.gnutella.routing.managers.QueryRoutingTableManager;
import org.protobee.gnutella.session.MessageReceivedEvent;
import org.protobee.gnutella.util.GUID;
import org.protobee.guice.SessionScope;
import org.protobee.identity.NetworkIdentityManager;
import org.protobee.modules.ProtocolModule;
import org.protobee.network.ProtocolMessageWriter;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;


@SessionScope
public class QueryHitModule implements ProtocolModule {

  private final RequestFilter filter;
  private final PushRoutingTableManager pushRTManager;
  private final QueryRoutingTableManager queryHitRTManager;
  private final ProtocolMessageWriter messageDispatcher;
  private final NetworkIdentityManager identityManager;
  private final MessageHeader.Factory headerFactory;
  private final GnutellaServantModel servantModel;

  @Inject
  public QueryHitModule(RequestFilter filter, PushRoutingTableManager pushRTManager,
      QueryRoutingTableManager queryHitRTManager, NetworkIdentityManager identityManager,
      ProtocolMessageWriter messageDispatcher, MessageHeader.Factory headerFactory,
      GnutellaServantModel servantModel) {
    this.filter = filter;
    this.pushRTManager = pushRTManager;
    this.queryHitRTManager = queryHitRTManager;
    this.identityManager = identityManager;
    this.messageDispatcher = messageDispatcher;
    this.headerFactory = headerFactory;
    this.servantModel = servantModel;
  }

  // map message header to an null when originally sending query
  private void queryHitMessageRecieved(MessageReceivedEvent event, MessageHeader header)
      throws InvalidMessageException {

    QueryHitBody queryHitBody = (QueryHitBody) event.getMessage().getBody();
    QueryGUIDRoutingPair qgrPair =
        queryHitRTManager.findRoutingForQuerys(new GUID(header.getGuid()),
            queryHitBody.getNumHits());
    IdentityHash queryHash = new IdentityHash(header.getGuid(), queryHitBody.getHuge().getUrns());

    if (!filter.shouldAcceptQueryHitMessage(qgrPair, servantModel.getGuid(),
        queryHitBody.getServantID(), queryHash)) {
      return;
    }

    queryHitRTManager.addQueryHit(queryHash);

    if (qgrPair.getHost() == identityManager.getMe()) { // if localhost, use locally

    } else {
      if (filter.shouldRouteQueryHitMessage(qgrPair, header.getTtl())) {
        pushRTManager.addRouting(queryHitBody.getServantID(), qgrPair.getHost());
        MessageHeader newHeader =
            headerFactory.create(header.getGuid(), MessageHeader.F_QUERY_REPLY,
                (byte) (header.getTtl() - 1), (byte) (header.getHops() + 1));
        messageDispatcher.write(qgrPair.getHost(), new GnutellaMessage(newHeader, event
            .getMessage().getBody()));
      }
    }
  }

  @Subscribe
  public void messageReceived(MessageReceivedEvent event) throws InvalidMessageException {
    MessageHeader header = event.getMessage().getHeader();
    if (header.getPayloadType() == MessageHeader.F_QUERY_REPLY) {
      queryHitMessageRecieved(event, header);
    }
  }
}

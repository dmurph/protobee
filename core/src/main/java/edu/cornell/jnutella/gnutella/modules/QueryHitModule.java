package edu.cornell.jnutella.gnutella.modules;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import edu.cornell.jnutella.gnutella.Gnutella;
import edu.cornell.jnutella.gnutella.GnutellaIdentityModel;
import edu.cornell.jnutella.gnutella.RequestFilter;
import edu.cornell.jnutella.gnutella.messages.GnutellaMessage;
import edu.cornell.jnutella.gnutella.messages.MessageHeader;
import edu.cornell.jnutella.gnutella.messages.QueryHitBody;
import edu.cornell.jnutella.gnutella.routing.IdentityHash;
import edu.cornell.jnutella.gnutella.routing.InvalidMessageException;
import edu.cornell.jnutella.gnutella.routing.QueryGUIDRoutingPair;
import edu.cornell.jnutella.gnutella.routing.managers.PushRoutingTableManager;
import edu.cornell.jnutella.gnutella.routing.managers.QueryRoutingTableManager;
import edu.cornell.jnutella.gnutella.session.MessageDispatcher;
import edu.cornell.jnutella.gnutella.session.MessageReceivedEvent;
import edu.cornell.jnutella.guice.SessionScope;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.identity.NetworkIdentityManager;
import edu.cornell.jnutella.modules.ProtocolModule;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.util.GUID;

@SessionScope
public class QueryHitModule implements ProtocolModule {

  private final RequestFilter filter;
  private final PushRoutingTableManager pushRTManager;
  private final QueryRoutingTableManager queryHitRTManager;
  private final MessageDispatcher messageDispatcher;
  private final NetworkIdentityManager identityManager;
  private final Protocol gnutella;
  private final MessageHeader.Factory headerFactory;

  @Inject
  public QueryHitModule(RequestFilter filter, PushRoutingTableManager pushRTManager, 
                        QueryRoutingTableManager queryHitRTManager, NetworkIdentityManager identityManager,
                        @Gnutella Protocol gnutella, MessageDispatcher messageDispatcher,
                        MessageHeader.Factory headerFactory) {
    this.filter = filter;
    this.pushRTManager = pushRTManager;
    this.queryHitRTManager = queryHitRTManager;
    this.identityManager = identityManager;
    this.gnutella = gnutella;
    this.messageDispatcher = messageDispatcher;
    this.headerFactory = headerFactory;
  }

  // map message header to an null when originally sending query
  private void queryHitMessageRecieved(MessageReceivedEvent event, MessageHeader header) throws InvalidMessageException {

    QueryHitBody queryHitBody =  (QueryHitBody) event.getMessage().getBody();
    QueryGUIDRoutingPair qgrPair = queryHitRTManager.findRoutingForQuerys(new GUID(header.getGuid()), queryHitBody.getNumHits());
    NetworkIdentity me = identityManager.getMe();
    GnutellaIdentityModel identityModel = (GnutellaIdentityModel) me.getModel(gnutella);
    IdentityHash queryHash = new IdentityHash(header.getGuid(), queryHitBody.getHuge().getUrns());

    if (!filter.shouldAcceptQueryHitMessage(qgrPair, identityModel.getGuid(), queryHitBody.getServantID(), queryHash )) { return; }

    queryHitRTManager.addQueryHit(queryHash);

    if (qgrPair.getHost() == identityManager.getMe()){ // if localhost, use locally

    }
    else{
      if (filter.shouldRouteQueryHitMessage(qgrPair, header.getTtl())){
        pushRTManager.addRouting( queryHitBody.getServantID(), qgrPair.getHost() );
        MessageHeader newHeader = headerFactory.create(header.getGuid(), MessageHeader.F_QUERY_REPLY, (byte) (header.getTtl() - 1), (byte) (header.getHops() - 1));
        messageDispatcher.dispatchMessage(qgrPair.getHost(), new GnutellaMessage(newHeader, event.getMessage().getBody()));
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

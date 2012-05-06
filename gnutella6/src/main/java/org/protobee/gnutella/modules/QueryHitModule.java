package org.protobee.gnutella.modules;

import org.protobee.compatability.Headers;
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
import org.protobee.guice.scopes.SessionScope;
import org.protobee.identity.NetworkIdentity;
import org.protobee.identity.NetworkIdentityManager;
import org.protobee.modules.ProtocolModule;
import org.protobee.network.ProtobeeMessageWriter;
import org.protobee.protocol.Protocol;
import org.protobee.stats.DropLog;
import org.protobee.network.ProtobeeMessageWriter.HandshakeOptions;
import org.protobee.session.SessionModel;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

@Headers(required = {})
@SessionScope
public class QueryHitModule extends ProtocolModule {

  private final RequestFilter filter;
  private final PushRoutingTableManager pushRTManager;
  private final QueryRoutingTableManager queryHitRTManager;
  private final ProtobeeMessageWriter messageDispatcher;
  private final NetworkIdentityManager identityManager;
  private final MessageHeader.Factory headerFactory;
  private final NetworkIdentity identity;
  private final Protocol gnutella;

  private final DropLog dropLog;

  private final SessionModel session;

  @Inject
  public QueryHitModule(RequestFilter filter, PushRoutingTableManager pushRTManager,
      QueryRoutingTableManager queryHitRTManager, NetworkIdentityManager identityManager,
      ProtobeeMessageWriter messageDispatcher, MessageHeader.Factory headerFactory,
      DropLog dropLog, GnutellaServantModel servantModel, SessionModel session,
      NetworkIdentity identity, Protocol gnutella) {
    this.filter = filter;
    this.pushRTManager = pushRTManager;
    this.queryHitRTManager = queryHitRTManager;
    this.identityManager = identityManager;
    this.messageDispatcher = messageDispatcher;
    this.headerFactory = headerFactory;
    this.dropLog = dropLog;
    this.identity = identity;
    this.gnutella = gnutella;
    this.session = session;
  }

  // map message header to an null when originally sending query
  private void queryHitMessageRecieved(MessageReceivedEvent event, MessageHeader header)
      throws InvalidMessageException {

    QueryHitBody queryHitBody = (QueryHitBody) event.getMessage().getBody();
    QueryGUIDRoutingPair qgrPair =
        queryHitRTManager.findRoutingForQuerys(new GUID(header.getGuid()),
            queryHitBody.getNumHits());

    IdentityHash queryHash = new IdentityHash(header.getGuid(), queryHitBody.getUrns());

    queryHitRTManager.addQueryHit(queryHash);

    if (qgrPair.getHost() == identityManager.getMe()) {
      // if localhost, use locally
    } else {
      String filterOutput = filter.shouldRouteQueryHitMessage(qgrPair, header.getTtl());
      if (filterOutput != null) {
        dropLog.messageDropped(identity.getSendingAddress(gnutella), gnutella, event.getMessage(),
            filterOutput);
        return;
      }
      pushRTManager.addRouting(queryHitBody.getServantID(), qgrPair.getHost());
      MessageHeader newHeader =
          headerFactory.create(header.getGuid(), MessageHeader.F_QUERY_REPLY,
              (byte) (header.getTtl() - 1), (byte) (header.getHops() + 1));
      session.exitScope();
      identity.exitScope();
      try {
        qgrPair.getHost().enterScope();
        messageDispatcher.write(new GnutellaMessage(newHeader, event.getMessage().getBody()),
            HandshakeOptions.WAIT_FOR_HANDSHAKE);
      } finally {
        qgrPair.getHost().exitScope();
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

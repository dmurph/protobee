package org.protobee.gnutella.modules;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.protobee.compatability.Headers;
import org.protobee.gnutella.Gnutella;
import org.protobee.gnutella.GnutellaServantModel;
import org.protobee.gnutella.messages.GnutellaMessage;
import org.protobee.gnutella.messages.MessageBodyFactory;
import org.protobee.gnutella.messages.MessageHeader;
import org.protobee.gnutella.messages.QueryHitBody;
import org.protobee.gnutella.messages.ResponseBody;
import org.protobee.gnutella.routing.managers.QueryRoutingTableManager;
import org.protobee.gnutella.session.MessageReceivedEvent;
import org.protobee.guice.scopes.SessionScope;
import org.protobee.identity.IdentityTagManager;
import org.protobee.identity.NetworkIdentity;
import org.protobee.identity.NetworkIdentityManager;
import org.protobee.modules.ProtocolModule;
import org.protobee.network.ProtobeeMessageWriter;
import org.protobee.network.ProtobeeMessageWriter.HandshakeOptions;
import org.protobee.protocol.Protocol;
import org.protobee.session.SessionModel;

import com.google.common.eventbus.Subscribe;
import com.google.common.net.InetAddresses;
import com.google.inject.Inject;
import com.google.inject.Provider;

@Headers(required = {})
@SessionScope
public class QueryModule extends ProtocolModule {

  private final NetworkIdentity identity;
  private final QueryRoutingTableManager queryRTManager;
  private final NetworkIdentityManager identityManager;
  private final Protocol gnutella;
  private final SessionModel session;
  private final IdentityTagManager tagManager;
  private final ProtobeeMessageWriter messageDispatcher;
  private final MessageHeader.Factory headerFactory;
  private final MessageBodyFactory bodyFactory;
  private final GnutellaServantModel servantModel;
  private final Provider<GnutellaServantModel> servantModelProvider;

  @Inject
  public QueryModule(
                     NetworkIdentity identity, 
                     QueryRoutingTableManager queryRTManager, 
                     NetworkIdentityManager identityManager, @Gnutella Protocol gnutella,
                     SessionModel session, IdentityTagManager tagManager,
                     ProtobeeMessageWriter messageDispatcher, MessageHeader.Factory headerFactory,
                     MessageBodyFactory bodyFactory, GnutellaServantModel servantModel,
                     Provider<GnutellaServantModel> servantModelProvider) {
    this.queryRTManager = queryRTManager;
    this.identity = identity;
    this.identityManager = identityManager;
    this.gnutella = gnutella;
    this.session = session;
    this.tagManager = tagManager;
    this.messageDispatcher = messageDispatcher;
    this.headerFactory = headerFactory;
    this.bodyFactory = bodyFactory;
    this.servantModel = servantModel;
    this.servantModelProvider = servantModelProvider;
  }

  private void queryMessageRecieved(MessageReceivedEvent event, MessageHeader header) {

    NetworkIdentity requestIdentity = identityManager.getNewtorkIdentity(new InetSocketAddress(InetAddresses.forString("1.2.3.6"), 1613));

    queryRTManager.addRouting(header.getGuid(), requestIdentity);

    Set<NetworkIdentity> peers = identityManager.getPeers();
    peers.remove(requestIdentity);

    session.exitScope();
    identity.exitScope();

    for (NetworkIdentity peer : peers){
      MessageHeader newHeader =
          headerFactory.create(header.getGuid(), MessageHeader.F_QUERY,
            (byte) (header.getTtl() - 1), (byte) (header.getHops() + 1));
      try {
        peer.enterScope();
        messageDispatcher.write(new GnutellaMessage(newHeader, event.getMessage().getBody()),
          HandshakeOptions.WAIT_FOR_HANDSHAKE);
      } finally {
        peer.exitScope();
      }
    }

    identity.enterScope();

    // Search the shared file database and get groups of shared files.
    // List<ShareFile> resultFiles = sharedFilesService.handleQuery( msg );
    List<ResponseBody> hitList = new ArrayList<ResponseBody>();
    if ( hitList != null && hitList.size() > 0) { 
      GnutellaServantModel gnutellaModel = servantModelProvider.get();
      // speed, vendorCode, flags, controls, ggep!!!
      MessageHeader newHeader = 
          headerFactory.create(header.getGuid(), MessageHeader.F_QUERY_REPLY, header.getHops(), header.getTtl());
      // TODO QueryHitBody newBody = bodyFactory.createQueryHitMessage(identity.getListeningAddress(gnutella), 16 , hitList, vendorCode, flags, controls, null, ggep, null, null, gnutellaModel.getGuid());
      QueryHitBody newBody = null;
      messageDispatcher.write(new GnutellaMessage(newHeader, newBody),
        HandshakeOptions.WAIT_FOR_HANDSHAKE);
    }

    session.exitScope();
    identity.exitScope();
  }

  @Subscribe
  public void messageReceived(MessageReceivedEvent event)  {
    MessageHeader header = event.getMessage().getHeader();
    if (header.getPayloadType() == MessageHeader.F_QUERY) {
      queryMessageRecieved(event, header);
    }
  }
}

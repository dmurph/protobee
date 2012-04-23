package edu.cornell.jnutella.gnutella.modules;

import java.net.InetSocketAddress;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import edu.cornell.jnutella.extension.GGEP;
import edu.cornell.jnutella.gnutella.Gnutella;
import edu.cornell.jnutella.gnutella.GnutellaIdentityModel;
import edu.cornell.jnutella.gnutella.RequestFilter;
import edu.cornell.jnutella.gnutella.messages.GnutellaMessage;
import edu.cornell.jnutella.gnutella.messages.MessageBodyFactory;
import edu.cornell.jnutella.gnutella.messages.MessageHeader;
import edu.cornell.jnutella.gnutella.messages.PongBody;
import edu.cornell.jnutella.gnutella.session.MessageDispatcher;
import edu.cornell.jnutella.gnutella.session.MessageReceivedEvent;
import edu.cornell.jnutella.guice.SessionScope;
import edu.cornell.jnutella.identity.IdentityTagManager;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.identity.NetworkIdentityManager;
import edu.cornell.jnutella.modules.ProtocolModule;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.headers.CompatabilityHeader;
import edu.cornell.jnutella.protocol.headers.Headers;

@Headers(requiredCompatabilities = {@CompatabilityHeader(name = "Pong-Caching", minVersion = "0.1", maxVersion = "+")}, requestedCompatabilities = {})
@SessionScope
public class PingModule implements ProtocolModule {

  private final RequestFilter filter;
  private final NetworkIdentityManager identityManager;
  private final IdentityTagManager tagManager;
  private final NetworkIdentity identity;
  private final MessageDispatcher messageDispatcher;
  private final MessageBodyFactory bodyFactory;
  private final MessageHeader.Factory headerFactory;
  private final Protocol gnutella;

  @Inject
  public PingModule(RequestFilter filter, NetworkIdentityManager identityManager,
      IdentityTagManager tagManager, NetworkIdentity identity, MessageDispatcher messageDispatcher,
      MessageBodyFactory bodyFactory, MessageHeader.Factory headerFactory,
      @Gnutella Protocol gnutella) {
    this.filter = filter;
    this.identityManager = identityManager;
    this.tagManager = tagManager;
    this.identity = identity;
    this.messageDispatcher = messageDispatcher;
    this.bodyFactory = bodyFactory;
    this.headerFactory = headerFactory;
    this.gnutella = gnutella;
  }

  public void pingMessageRecieved(MessageReceivedEvent event, MessageHeader header) {
    // to reduce the incoming connection attempts of other clients
    // only response to ping a when we have free incoming slots or this
    // ping has a original TTL ( current TTL + hops ) of 2.
    byte ttl = header.getTtl();
    byte hops = header.getHops();
    if (!filter.shouldAcceptPing(event.getMessage())) {
      return;
    }

    // For crawler pings (hops==0, ttl=2) we have a special treatment...
    // We reply with all our leaf connections... in case we have them as a
    // ultrapeer...
    if (hops == 0 && ttl == 2) {// crawler ping
                                // respond with leaf nodes pongs, already "hoped" one step.
                                // (ttl=1,hops=1)
      Set<NetworkIdentity> leafs = identityManager.getTaggedIdentities(tagManager.getLeafKey());

      for (NetworkIdentity leaf : leafs) {
        if (leaf == identity) {
          continue;
        }
        GnutellaIdentityModel gnutellaModel = (GnutellaIdentityModel) leaf.getModel(gnutella);
        MessageHeader newHeader =
            headerFactory.create(header.getGuid(), MessageHeader.F_PING_REPLY, (byte) 1, (byte) 0);
        PongBody body =
            bodyFactory.createPongMessage(gnutellaModel.getNetworkAddress(),
                gnutellaModel.getFileCount(), gnutellaModel.getFileSizeInKB(), null);
        messageDispatcher.dispatchMessage(new GnutellaMessage(newHeader, body));
      }
    }

    if (ttl == 1 && hops <= 1) {
      // just send our data back
      NetworkIdentity me = identityManager.getMe();
      GnutellaIdentityModel identityModel = (GnutellaIdentityModel) me.getModel(gnutella);
      InetSocketAddress address = (InetSocketAddress) identityModel.getNetworkAddress();
      Preconditions.checkState(address != null, "Host address of program must be populated");

      byte newTTL = (byte) (hops + ttl);
      MessageHeader newHeader =
          headerFactory.create(header.getGuid(), MessageHeader.F_PING_REPLY, (byte) newTTL,
              (byte) 0);

      GGEP ggep = new GGEP();
      // TODO populate ggep
      PongBody body =
          bodyFactory.createPongMessage(address, identityModel.getFileCount(),
              identityModel.getFileSizeInKB(), ggep);
      messageDispatcher.dispatchMessage(new GnutellaMessage(newHeader, body));

    } else if (ttl > 1) {
      // send cached pongs if we have enough, otherwise we dispatch to neighbors
    }

    // send back my own pong
    // byte newTTL = hops++;
    // if ((hops + ttl) <= 2) {
    // newTTL = 1;
    // }
    //
    // int avgDailyUptime = ((Integer) uptimeStatsProvider.getValue()).intValue();
    // int shareFileCount = sharedFilesService.getFileCount();
    // int shareFileSize = sharedFilesService.getTotalFileSizeInKb();
    //
    // // Get my host:port for InitResponse.
    // PongMsg pong =
    // pongFactory.createMyOutgoingPong(header.getMsgID(), servent.getLocalAddress(), newTTL,
    // shareFileCount, shareFileSize, servent.isUltrapeer(), avgDailyUptime,
    // sourceHost.isGgepSupported());
    // sourceHost.queueMessageToSend(pong);
    //
    // // send pongs from pong cache
    // DestAddress orginAddress = sourceHost.getHostAddress();
    // IpAddress ip = orginAddress.getIpAddress();
    // if (ip == null) {
    // return;
    // }
    // GUID guid = header.getMsgID();
    // List<PongMsg> pongs = servent.getMessageService().getCachedPongs();
    // for (PongMsg pMsg : pongs) {
    // if (ip.equals(pMsg.getPongAddress().getIpAddress())) {
    // continue;
    // }
    // PongMsg pongCpy =
    // pongFactory.createFromCachePong(guid, newTTL, pMsg, sourceHost.isGgepSupported());
    // sourceHost.queueMessageToSend(pongCpy);
    // }
  }

  @Subscribe
  public void messageReceived(MessageReceivedEvent event) {
    MessageHeader header = event.getMessage().getHeader();
    if (header.getPayloadType() == MessageHeader.F_PING) {
      pingMessageRecieved(event, header);
    }


  }
}

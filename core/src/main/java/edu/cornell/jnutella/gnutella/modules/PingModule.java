package edu.cornell.jnutella.gnutella.modules;

import java.util.Set;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import edu.cornell.jnutella.gnutella.RequestFilter;
import edu.cornell.jnutella.gnutella.messages.GnutellaMessage;
import edu.cornell.jnutella.gnutella.messages.MessageHeader;
import edu.cornell.jnutella.gnutella.session.MessageReceivedEvent;
import edu.cornell.jnutella.guice.SessionScope;
import edu.cornell.jnutella.identity.IdentityTagManager;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.identity.NetworkIdentityManager;
import edu.cornell.jnutella.modules.ProtocolModule;

@SessionScope
public class PingModule implements ProtocolModule {

  private final RequestFilter filter;
  private final NetworkIdentityManager identityManager;
  private final IdentityTagManager tagManager;
  private final NetworkIdentity identity;

  @Inject
  public PingModule(RequestFilter filter, NetworkIdentityManager identityManager,
                          IdentityTagManager tagManager, NetworkIdentity identity) {
    this.filter = filter;
    this.identityManager = identityManager;
    this.tagManager = tagManager;
    this.identity = identity;
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

    // For crawler pings (hops==1, ttl=1) we have a special treatment...
    // We reply with all our leaf connections... in case we have them as a
    // ultrapeer...
    if (hops == 0 && ttl == 2) {// crawler ping
                                // respond with leaf nodes pongs, already "hoped" one step.
                                // (ttl=1,hops=1)
      Set<NetworkIdentity> leafs = identityManager.getTaggedIdentities(tagManager.getLeafKey());
      for (NetworkIdentity leaf : leafs) {
        if(leaf == identity) {
          continue;
        }
        
      }
//      for (int i = 0; i < leafs.length; i++) {
//        DestAddress ha = leafs[i].getHostAddress();
//        PongMsg pong =
//            pongFactory.createOtherLeafsOutgoingPong(header.getMsgID(), (byte) 1, (byte) 1, ha);
//        sourceHost.queueMessageToSend(pong);
//      }
    }

//    // send back my own pong
//    byte newTTL = hops++;
//    if ((hops + ttl) <= 2) {
//      newTTL = 1;
//    }
//
//    int avgDailyUptime = ((Integer) uptimeStatsProvider.getValue()).intValue();
//    int shareFileCount = sharedFilesService.getFileCount();
//    int shareFileSize = sharedFilesService.getTotalFileSizeInKb();
//
//    // Get my host:port for InitResponse.
//    PongMsg pong =
//        pongFactory.createMyOutgoingPong(header.getMsgID(), servent.getLocalAddress(), newTTL,
//            shareFileCount, shareFileSize, servent.isUltrapeer(), avgDailyUptime,
//            sourceHost.isGgepSupported());
//    sourceHost.queueMessageToSend(pong);
//
//    // send pongs from pong cache
//    DestAddress orginAddress = sourceHost.getHostAddress();
//    IpAddress ip = orginAddress.getIpAddress();
//    if (ip == null) {
//      return;
//    }
//    GUID guid = header.getMsgID();
//    List<PongMsg> pongs = servent.getMessageService().getCachedPongs();
//    for (PongMsg pMsg : pongs) {
//      if (ip.equals(pMsg.getPongAddress().getIpAddress())) {
//        continue;
//      }
//      PongMsg pongCpy =
//          pongFactory.createFromCachePong(guid, newTTL, pMsg, sourceHost.isGgepSupported());
//      sourceHost.queueMessageToSend(pongCpy);
//    }
  }

  @Subscribe
  public void messageReceived(MessageReceivedEvent event) {
    MessageHeader header = event.getMessage().getHeader();
    if (header.getPayloadType() == MessageHeader.F_PING) {
      pingMessageRecieved(event, header);
    }


  }
}

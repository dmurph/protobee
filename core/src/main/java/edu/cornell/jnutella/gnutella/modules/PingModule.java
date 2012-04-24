package edu.cornell.jnutella.gnutella.modules;

import java.util.Set;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import edu.cornell.jnutella.gnutella.Gnutella;
import edu.cornell.jnutella.gnutella.GnutellaIdentityModel;
import edu.cornell.jnutella.gnutella.RequestFilter;
import edu.cornell.jnutella.gnutella.messages.GnutellaMessage;
import edu.cornell.jnutella.gnutella.messages.MessageBodyFactory;
import edu.cornell.jnutella.gnutella.messages.MessageHeader;
import edu.cornell.jnutella.gnutella.messages.PongBody;
import edu.cornell.jnutella.gnutella.session.MessageReceivedEvent;
import edu.cornell.jnutella.guice.SessionScope;
import edu.cornell.jnutella.identity.IdentityTagManager;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.identity.NetworkIdentityManager;
import edu.cornell.jnutella.modules.ProtocolModule;
import edu.cornell.jnutella.network.ProtocolMessageWriter;
import edu.cornell.jnutella.protocol.Protocol;

@SessionScope
public class PingModule implements ProtocolModule {

  private final RequestFilter filter;
  private final NetworkIdentityManager identityManager;
  private final IdentityTagManager tagManager;
  private final NetworkIdentity identity;
  private final ProtocolMessageWriter messageDispatcher;
  private final MessageBodyFactory bodyFactory;
  private final MessageHeader.Factory headerFactory;
  private final Protocol gnutella;

  @Inject
  public PingModule(RequestFilter filter, NetworkIdentityManager identityManager,
      IdentityTagManager tagManager, NetworkIdentity identity,
      ProtocolMessageWriter messageDispatcher, MessageBodyFactory bodyFactory,
      MessageHeader.Factory headerFactory, @Gnutella Protocol gnutella) {
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
        messageDispatcher.write(leaf, new GnutellaMessage(newHeader, body));
      }
    }

    if (ttl == 1 && hops <= 1) {
      // just send our data back
      NetworkIdentity me = identityManager.getMe();

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

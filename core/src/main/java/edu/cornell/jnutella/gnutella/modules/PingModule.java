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
import edu.cornell.jnutella.gnutella.routing.managers.PingRoutingTableManager;
import edu.cornell.jnutella.gnutella.session.MessageReceivedEvent;
import edu.cornell.jnutella.guice.SessionScope;
import edu.cornell.jnutella.identity.IdentityTagManager;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.identity.NetworkIdentityManager;
import edu.cornell.jnutella.modules.ProtocolModule;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.headers.CompatabilityHeader;
import edu.cornell.jnutella.protocol.headers.Headers;

@Headers(required = {@CompatabilityHeader(name = "Pong-Caching", minVersion = "0.1", maxVersion = "+")}, requested = {})
@SessionScope
public class PingModule<ProtocolMessageWriter> implements ProtocolModule {

  private final RequestFilter filter;
  private final NetworkIdentityManager identityManager;
  private final IdentityTagManager tagManager;
  private final NetworkIdentity identity;
  private final ProtocolMessageWriter messageDispatcher;
  private final MessageBodyFactory bodyFactory;
  private final MessageHeader.Factory headerFactory;
  private final Protocol gnutella;
  private final PongCache pongCache;
  private final int cacheTimeout;
  private final int cacheThreshold;
  private final PingRoutingTableManager pingRTManager;

  @Inject
  public PingModule(RequestFilter filter, NetworkIdentityManager identityManager,
                    IdentityTagManager tagManager, NetworkIdentity identity,
                    ProtocolMessageWriter messageDispatcher, MessageBodyFactory bodyFactory,
                    MessageHeader.Factory headerFactory, @Gnutella Protocol gnutella, PongCache cache,
                    @PongCacheThreshold int threshold, @PongCacheTimout int timeout
                    PingRoutingTableManager pingRTManager) {
    this.filter = filter;
    this.identityManager = identityManager;
    this.tagManager = tagManager;
    this.identity = identity;
    this.messageDispatcher = messageDispatcher;
    this.bodyFactory = bodyFactory;
    this.headerFactory = headerFactory;
    this.gnutella = gnutella;
    this.pongCache = cache;
    this.cacheTimeout = timeout;
    this.cacheThreshold = threshold;
    this.pingRTManager = pingRTManager;
  }

  @Subscribe
  public void messageReceived(MessageReceivedEvent event) {
    MessageHeader header = event.getMessage().getHeader();
    switch (header.getPayloadType()) {
      case MessageHeader.F_PING:
        pingMessageRecieved(event);
        break;
      case MessageHeader.F_PING_REPLY:
        pongMessageReceived(event);
        break;
    }
  }

  public void pingMessageRecieved(MessageReceivedEvent event) {
    final GnutellaMessage message = event.getMessage();
    final MessageHeader header = message.getHeader();

    byte ttl = header.getTtl();
    byte hops = header.getHops();
    if (!filter.shouldAcceptPing(event.getMessage())) {
      return;
    }
    byte newTTL = (byte) (hops + ttl);

    // dispatch our pong
    {
      NetworkIdentity me = identityManager.getMe();
      GnutellaIdentityModel identityModel = (GnutellaIdentityModel) me.getModel(gnutella);
      InetSocketAddress address = (InetSocketAddress) identityModel.getNetworkAddress();
      Preconditions.checkState(address != null, "Host address of program must be populated");


      MessageHeader newHeader =
          headerFactory.create(header.getGuid(), MessageHeader.F_PING_REPLY, (byte) newTTL,
            (byte) 0);

      GGEP ggep = new GGEP();
      // TODO populate ggep
      PongBody newBody =
          bodyFactory.createPongMessage(address, identityModel.getFileCount(),
            identityModel.getFileSizeInKB(), ggep);
      messageDispatcher.write(new GnutellaMessage(newHeader, newBody));
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
        PongBody newBody =
            bodyFactory.createPongMessage(gnutellaModel.getNetworkAddress(),
              gnutellaModel.getFileCount(), gnutellaModel.getFileSizeInKB(), null);
        messageDispatcher.write(new GnutellaMessage(newHeader, newBody));
      }
    } else if (ttl > 1) {
      pongCache.filter(cacheTimeout);

      // send cached pongs if we have enough, otherwise we dispatch to neighbors
      if (pongCache.size() >= cacheThreshold) {
        Iterable<PongBody> pongs = pongCache.getPongs(cacheThreshold);

        for (PongBody body : pongs) {
          MessageHeader newHeader =
              headerFactory.create(header.getGuid(), MessageHeader.F_PING_REPLY, newTTL, (byte) 0);
          messageDispatcher.write(new GnutellaMessage(newHeader, body));
        }
      } else {
        Set<NetworkIdentity> leafs = identityManager.getTaggedIdentities(tagManager.getLeafKey());

        // TODO set up the ping routing here?
        if (pingRTManager.isRouted(header.getGUID())){ return;}
        
        int i = 0;
        for (NetworkIdentity leaf : leafs) {
          if (i > cacheThreshold) {
            break;
          }
          if (leaf == identity) {
            continue;
          }
          MessageHeader newHeader =
              headerFactory.create(header.getGuid(), MessageHeader.F_PING, (byte) (ttl - 1),
                (byte) (hops + 1), header.getPayloadLength());
          messageDispatcher.write(leaf, new GnutellaMessage(newHeader, message.getBody()));
          i++;
        }
      }
    }
  }

  public void pongMessageReceived(MessageReceivedEvent event) {

  }


}
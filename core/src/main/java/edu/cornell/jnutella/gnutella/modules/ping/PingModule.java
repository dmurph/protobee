package edu.cornell.jnutella.gnutella.modules.ping;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
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
import edu.cornell.jnutella.gnutella.modules.MaxTTL;
import edu.cornell.jnutella.gnutella.modules.ping.AdvancedPongCache.CacheEntry;
import edu.cornell.jnutella.gnutella.session.MessageReceivedEvent;
import edu.cornell.jnutella.guice.SessionScope;
import edu.cornell.jnutella.identity.IdentityTagManager;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.identity.NetworkIdentityManager;
import edu.cornell.jnutella.modules.ProtocolModule;
import edu.cornell.jnutella.network.ProtocolMessageWriter;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.headers.CompatabilityHeader;
import edu.cornell.jnutella.protocol.headers.Headers;
import edu.cornell.jnutella.util.Clock;
import edu.cornell.jnutella.util.GUID;

@Headers(required = {@CompatabilityHeader(name = "Pong-Caching", minVersion = "0.1", maxVersion = "+")}, requested = {})
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

  private final Clock clock;
  private final AdvancedPongCache pongCache;
  private final PingSessionModel pingModel;
  private final int maxPongsSent;
  private final int expireTime;
  private final int maxTtl;

  @Inject
  public PingModule(RequestFilter filter, NetworkIdentityManager identityManager,
      IdentityTagManager tagManager, NetworkIdentity identity,
      ProtocolMessageWriter messageDispatcher, MessageBodyFactory bodyFactory,
      MessageHeader.Factory headerFactory, @Gnutella Protocol gnutella, PongCache cache,
      @MaxPongsSent int threshold, @MaxTTL int maxTtl, @PongExpireTime int expireTime,
      PingSessionModel pingModel, AdvancedPongCache pongCache, Clock clock) {
    this.filter = filter;
    this.identityManager = identityManager;
    this.tagManager = tagManager;
    this.identity = identity;
    this.messageDispatcher = messageDispatcher;
    this.bodyFactory = bodyFactory;
    this.headerFactory = headerFactory;
    this.gnutella = gnutella;
    this.pongCache = pongCache;
    this.pingModel = pingModel;
    this.maxTtl = maxTtl;
    this.maxPongsSent = threshold;
    this.clock = clock;
    this.expireTime = expireTime;
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

    byte inTtl = header.getTtl();
    byte inHops = header.getHops();

    // throttling
    long now = clock.currentTimeMillis();
    if (now < pingModel.getAcceptTime()) {
      // TODO: record drop
      return;
    }
    pingModel.setAcceptTime(now + expireTime);

    // responding
    if (pongCache.needsRebroadcasting()) {
      // get all current sessions, send pings to them with max ttl
    }

    // For crawler pings (hops==0, ttl=2)
    if (inHops == 0 && inTtl == 2) {
      // crawler ping
      // respond with leaf nodes pongs, already "hoped" one step.
      // (ttl=1,hops=1)
      Set<NetworkIdentity> leafs = identityManager.getTaggedIdentities(tagManager.getLeafKey());

      for (NetworkIdentity leaf : leafs) {
        if (leaf == identity) {
          continue;
        }
        GnutellaIdentityModel gnutellaModel = (GnutellaIdentityModel) leaf.getModel(gnutella);
        MessageHeader newHeader =
            headerFactory.create(header.getGuid(), MessageHeader.F_PING_REPLY, (byte) 1, (byte) 1);
        PongBody newBody =
            bodyFactory.createPongMessage(gnutellaModel.getNetworkAddress(),
                gnutellaModel.getFileCount(), gnutellaModel.getFileSizeInKB(), null);
        messageDispatcher.write(new GnutellaMessage(newHeader, newBody));
      }
      return;
    }

    // setup demultiplexing table
    pingModel.setAcceptGuid(header.getGuid());
    for (int i = 1; i <= maxTtl; i++) {
      int needed = 0;
      if (i <= header.getTtl()) {
        needed = maxPongsSent / header.getTtl();
      }
      pingModel.getNeeded()[i].set(needed);
    }

    byte newTtl = (byte) (inHops + 1);
    // dispatch our pong TODO: IF WE CAN ACCEPT CONNECTIONS
    {
      NetworkIdentity me = identityManager.getMe();
      GnutellaIdentityModel identityModel = (GnutellaIdentityModel) me.getModel(gnutella);
      InetSocketAddress address = (InetSocketAddress) identityModel.getNetworkAddress();
      Preconditions.checkState(address != null, "Host address of program must be populated");


      MessageHeader newHeader =
          headerFactory.create(header.getGuid(), MessageHeader.F_PING_REPLY, newTtl);

      GGEP ggep = new GGEP();
      // TODO populate ggep
      PongBody newBody =
          bodyFactory.createPongMessage(address, identityModel.getFileCount(),
              identityModel.getFileSizeInKB(), ggep);
      messageDispatcher.write(new GnutellaMessage(newHeader, newBody));
    }

    // send pongs that we have
    if (inTtl > 1) {

      List<GnutellaMessage> pongs = Lists.newArrayList();

      for (int ttl = 1; ttl <= header.getTtl(); ttl++) {
        int needed = pingModel.getNeeded()[ttl].get();

        synchronized (pongCache.getCacheLock()) {
          List<CacheEntry> list = pongCache.getCache()[ttl];

          for (int j = 0; needed > 0 && j < list.size(); j++) {
            CacheEntry entry = list.get(j);
            if (entry.identity == identity) {
              continue;
            }
            pongs.add(new GnutellaMessage(new MessageHeader(header.getGuid(),
                MessageHeader.F_PING_REPLY, (byte) 1, (byte) (ttl - 1)), entry.body));

            needed = pingModel.getNeeded()[ttl].decrementAndGet();
          }
        }
      }

      for (GnutellaMessage gnutellaMessage : pongs) {
        messageDispatcher.write(gnutellaMessage);
      }
    }
  }

  public void pongMessageReceived(MessageReceivedEvent event) {
    final GnutellaMessage message = event.getMessage();
    final MessageHeader header = message.getHeader();
    final PongBody body = (PongBody) message.getBody();

    if (!GUID.isModernClient(header.getGuid())) {
      pongCache.addToReservePongs(body);
    }

    synchronized (pongCache.getCacheLock()) {
      pongCache.getCache()[header.getHops()].add(new CacheEntry(body, identity));
    }
    
    // TODO: demultiplex, need to see all current connections
  }


}

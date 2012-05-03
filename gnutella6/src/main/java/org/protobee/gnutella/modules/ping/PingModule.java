package org.protobee.gnutella.modules.ping;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;

import org.protobee.compatability.CompatabilityHeader;
import org.protobee.compatability.Headers;
import org.protobee.gnutella.Gnutella;
import org.protobee.gnutella.GnutellaServantModel;
import org.protobee.gnutella.SlotsController;
import org.protobee.gnutella.constants.MaxTTL;
import org.protobee.gnutella.extension.GGEP;
import org.protobee.gnutella.messages.GnutellaMessage;
import org.protobee.gnutella.messages.MessageBodyFactory;
import org.protobee.gnutella.messages.MessageHeader;
import org.protobee.gnutella.messages.PongBody;
import org.protobee.gnutella.modules.ping.AdvancedPongCache.CacheEntry;
import org.protobee.gnutella.session.MessageReceivedEvent;
import org.protobee.gnutella.util.GUID;
import org.protobee.guice.scopes.SessionScope;
import org.protobee.identity.IdentityTagManager;
import org.protobee.identity.NetworkIdentity;
import org.protobee.identity.NetworkIdentityManager;
import org.protobee.modules.ProtocolModule;
import org.protobee.network.ProtobeeMessageWriter;
import org.protobee.network.ProtobeeMessageWriter.HandshakeOptions;
import org.protobee.protocol.Protocol;
import org.protobee.session.SessionManager;
import org.protobee.session.SessionModel;
import org.protobee.stats.DropLog;
import org.protobee.util.Clock;
import org.protobee.util.Descoper;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;


/**
 * Module for handling ping and pong messages. Preconditions: all messages are valid messages. We
 * are in our respective session and identity scopes on injection
 * 
 * @author Daniel
 */
@Headers(required = {@CompatabilityHeader(name = "Pong-Caching", minVersion = "0.1", maxVersion = "+")}, requested = {})
@SessionScope
public class PingModule extends ProtocolModule {

  private final NetworkIdentityManager identityManager;
  private final IdentityTagManager tagManager;
  private final SessionManager sessionManager;
  private final SlotsController slots;

  private final NetworkIdentity identity;
  private final SessionModel thisSession;
  private final ProtobeeMessageWriter messageDispatcher;
  private final MessageBodyFactory bodyFactory;
  private final MessageHeader.Factory headerFactory;
  private final Protocol gnutella;
  private final Descoper descoper;

  private final Provider<GnutellaServantModel> servantModelProvider;
  private final Provider<PingSessionModel> pingModelProvider;

  private final Clock clock;
  private final AdvancedPongCache pongCache;
  private final PingSessionModel pingModel;
  private final int maxPongsSent;
  private final int expireTime;
  private final int maxTtl;

  private final DropLog dropLog;

  @Inject
  public PingModule(NetworkIdentityManager identityManager, IdentityTagManager tagManager,
      NetworkIdentity identity, ProtobeeMessageWriter messageDispatcher,
      MessageBodyFactory bodyFactory, MessageHeader.Factory headerFactory,
      @Gnutella Protocol gnutella, @MaxPongsSent int threshold, @MaxTTL int maxTtl,
      @PongExpireTime int expireTime, PingSessionModel pingModel, AdvancedPongCache pongCache,
      Clock clock, Descoper descoper, Provider<GnutellaServantModel> servantProvider,
      SessionManager sessionManager, SlotsController slots, DropLog dropLog,
      Provider<PingSessionModel> pingModelProvider, SessionModel thisSession) {
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
    this.descoper = descoper;
    this.servantModelProvider = servantProvider;
    this.sessionManager = sessionManager;
    this.slots = slots;
    this.dropLog = dropLog;
    this.pingModelProvider = pingModelProvider;
    this.thisSession = thisSession;
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

  private GnutellaMessage createMePing(MessageHeader header) {
    // TODO populate pong ggep
    return new GnutellaMessage(header, bodyFactory.createPingMessage(null));
  }

  private void pingMessageRecieved(MessageReceivedEvent event) {
    final GnutellaMessage message = event.getMessage();
    final MessageHeader header = message.getHeader();

    byte inTtl = header.getTtl();
    byte inHops = header.getHops();

    // throttling
    long now = clock.currentTimeMillis();
    if (now < pingModel.getAcceptTime()) {
      dropLog.messageDropped(identity.getSendingAddress(gnutella), gnutella, message,
          "Ping was sent before min expire time after last ping");
      return;
    }
    pingModel.setAcceptTime(now + expireTime);

    // responding

    if (inTtl > 1) {
      // only need other pongs if our ttl > 1
      if (pongCache.needsRebroadcasting()) {
        // get all current sessions, send pings to them with max ttl
        Set<SessionModel> sessions = sessionManager.getCurrentSessions(gnutella);
        for (SessionModel session : sessions) {
          MessageHeader newHeader =
              headerFactory.create(new GUID().getBytes(), MessageHeader.F_PING, (byte) maxTtl);
          thisSession.exitScope();
          identity.exitScope();
          try {
            session.getIdentity().enterScope();
            messageDispatcher.write(createMePing(newHeader), HandshakeOptions.WAIT_FOR_HANDSHAKE);
          } finally {
            session.getIdentity().exitScope();
          }
          identity.exitScope();
          thisSession.exitScope();
        }
      }
    }

    // For crawler pings (hops==0, ttl=2)
    if (inHops == 0 && inTtl == 2) {
      // crawler ping
      // respond with leaf nodes pongs, already "hoped" one step.
      // (ttl=1,hops=1)
      Set<NetworkIdentity> leafs = identityManager.getTaggedIdentities(tagManager.getLeafKey());
      try {
        descoper.descope();
        for (NetworkIdentity leaf : leafs) {
          if (leaf == identity) {
            continue;
          }
          try {
            leaf.enterScope();
            GnutellaServantModel gnutellaModel = servantModelProvider.get();
            MessageHeader newHeader =
                headerFactory.create(header.getGuid(), MessageHeader.F_PING_REPLY, (byte) 1,
                    (byte) 1);
            PongBody newBody =
                bodyFactory.createPongMessage((InetSocketAddress) leaf.getSendingAddress(gnutella),
                    gnutellaModel.getFileCount(), gnutellaModel.getFileSizeInKB(), null);
            messageDispatcher.write(new GnutellaMessage(newHeader, newBody));
          } finally {
            leaf.exitScope();
          }
        }
      } finally {
        descoper.rescope();
      }
      return;
    }

    // setup demultiplexing table
    // we should only setting non-zero values when ttl is > 1
    pingModel.setAcceptGuid(header.getGuid());
    for (int i = 1; i <= maxTtl; i++) {
      int needed = 0;
      // we do < because we need to 'shift' what the remote wants by one, to remove
      // ttl = 1, which means just us.
      if (i < header.getTtl()) {
        // since i starts at one, header ttl has to be at least 2 here
        needed = maxPongsSent / (header.getTtl() - 1);
      }
      pingModel.getNeeded()[i].set(needed);
    }

    byte newTtl = (byte) (inHops + 1);
    // dispatch our pong
    if (slots.canAcceptNewConnection()) {

      NetworkIdentity me = identityManager.getMe();
      try {
        descoper.descope();
        me.enterScope();

        GnutellaServantModel identityModel = servantModelProvider.get();
        InetSocketAddress address = (InetSocketAddress) me.getListeningAddress(gnutella);
        Preconditions.checkState(address != null, "Host address of program must be populated");

        MessageHeader newHeader =
            headerFactory.create(header.getGuid(), MessageHeader.F_PING_REPLY, newTtl);

        GGEP ggep = new GGEP();
        // TODO populate ggep
        PongBody newBody =
            bodyFactory.createPongMessage(address, identityModel.getFileCount(),
                identityModel.getFileSizeInKB(), ggep);
        messageDispatcher.write(new GnutellaMessage(newHeader, newBody));
      } finally {
        me.exitScope();
        descoper.rescope();
      }
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
                MessageHeader.F_PING_REPLY, (byte) 1, (byte) ttl), entry.body));

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

    byte hops = (byte) (header.getHops() + 1);
    synchronized (pongCache.getCacheLock()) {
      pongCache.getCache()[hops].add(new CacheEntry(body, identity));
    }

    Set<SessionModel> sessions = sessionManager.getCurrentSessions(gnutella);
    for (SessionModel sessionModel : sessions) {
      if (sessionModel.getIdentity() == identity) {
        continue;
      }
      thisSession.exitScope();
      identity.exitScope();
      sessionModel.enterScope();
      PingSessionModel pingModel = pingModelProvider.get();
      sessionModel.exitScope();
      if (pingModel.getNeeded()[hops].get() > 0) {
        pingModel.getNeeded()[hops].decrementAndGet();
        GnutellaMessage newMessage =
            new GnutellaMessage(headerFactory.create(pingModel.getAcceptGuid(),
                MessageHeader.F_PING_REPLY, (byte) 1, hops), body);
        try {
          sessionModel.getIdentity().enterScope();

          messageDispatcher.write(newMessage, HandshakeOptions.EXIT_IF_HANDSHAKING);
        } finally {
          sessionModel.getIdentity().exitScope();
        }
      }
      identity.enterScope();
      thisSession.enterScope();
    }
  }
}

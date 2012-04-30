package org.protobee.gnutella.modules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.protobee.gnutella.AbstractGnutellaTest;
import org.protobee.gnutella.GnutellaServantModel;
import org.protobee.gnutella.SlotsController;
import org.protobee.gnutella.constants.MaxTTL;
import org.protobee.gnutella.extension.GGEP;
import org.protobee.gnutella.messages.GnutellaMessage;
import org.protobee.gnutella.messages.MessageHeader;
import org.protobee.gnutella.messages.PingBody;
import org.protobee.gnutella.messages.PongBody;
import org.protobee.gnutella.modules.ping.AdvancedPongCache;
import org.protobee.gnutella.modules.ping.AdvancedPongCache.CacheEntry;
import org.protobee.gnutella.modules.ping.MaxPongsSent;
import org.protobee.gnutella.modules.ping.PingModule;
import org.protobee.gnutella.modules.ping.PingSessionModel;
import org.protobee.gnutella.modules.ping.PongExpireTime;
import org.protobee.gnutella.session.MessageReceivedEvent;
import org.protobee.gnutella.util.GUID;
import org.protobee.identity.IdentityTagManager;
import org.protobee.identity.NetworkIdentity;
import org.protobee.identity.NetworkIdentityManager;
import org.protobee.network.ProtocolMessageWriter;
import org.protobee.network.ProtocolMessageWriter.ConnectionOptions;
import org.protobee.network.ProtocolMessageWriter.HandshakeOptions;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolConfig;
import org.protobee.session.SessionManagerImpl;
import org.protobee.session.SessionModel;
import org.protobee.stats.DropLog;
import org.protobee.util.Clock;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.net.InetAddresses;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;


public class PingModuleTests extends AbstractGnutellaTest {

  @Test
  public void testDropOnFastRepeat() {
    final DropLog dropLog = mock(DropLog.class);
    final Clock clock = mock(Clock.class);
    final ProtocolMessageWriter writer = mock(ProtocolMessageWriter.class);

    final int expireTime = 6000;
    final long firstAcceptTime = 3000;
    final long firstTime = firstAcceptTime - 1;
    Injector inj = getInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(ProtocolMessageWriter.class).toInstance(writer);
        bind(DropLog.class).toInstance(dropLog);
        bind(Clock.class).toInstance(clock);
        bindConstant().annotatedWith(PongExpireTime.class).to(expireTime);
      }
    });
    when(clock.currentTimeMillis()).thenReturn(firstTime);

    ProtocolConfig gnutellaConfig = getGnutellaProtocolConfig(inj);
    SocketAddress address = createAddress("3.2.4.1", 50);
    SessionModel session = createSession(inj, address, gnutellaConfig);
    NetworkIdentity identity = session.getIdentity();

    identity.enterScope();
    session.enterScope();
    PingSessionModel pingModel = inj.getInstance(PingSessionModel.class);
    pingModel.setAcceptTime(firstAcceptTime);


    PingModule module = inj.getInstance(PingModule.class);

    GnutellaMessage message =
        new GnutellaMessage(new MessageHeader(new byte[16], MessageHeader.F_PING, (byte) 1,
            (byte) 0), null);
    module.messageReceived(new MessageReceivedEvent(null, message));

    session.exitScope();
    identity.exitScope();

    verify(dropLog).messageDropped(eq(address), eq(gnutellaConfig.get()), eq(message), any(String.class));
  }

  @Test
  public void testBroadcastOnEmptyCache() {
    final ProtocolMessageWriter writer = mock(ProtocolMessageWriter.class);
    final SessionManagerImpl sessionManager = mock(SessionManagerImpl.class);
    final SlotsController slots = mock(SlotsController.class);
    when(slots.canAcceptNewConnection()).thenReturn(false);
    // so we're set up to not accept any new connections

    final int maxTtl = 5;
    Injector inj = getInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(ProtocolMessageWriter.class).toInstance(writer);
        bind(SessionManagerImpl.class).toInstance(sessionManager);
        bind(SlotsController.class).toInstance(slots);
        bindConstant().annotatedWith(MaxTTL.class).to(maxTtl);
      }
    });

    // create our sessions
    ProtocolConfig gnutellaConfig = getGnutellaProtocolConfig(inj);
    Set<SessionModel> sessions =
        Sets.newHashSet(createSession(inj, createAddress("1.2.3.4", 10), gnutellaConfig),
            createSession(inj, createAddress("1.2.3.4", 20), gnutellaConfig),
            createSession(inj, createAddress("1.2.5.5", 10), gnutellaConfig));

    when(sessionManager.getCurrentSessions(any(Protocol.class))).thenReturn(sessions);


    SocketAddress remoteAddress = new InetSocketAddress(InetAddresses.forString("1.2.3.6"), 1613);
    SessionModel pingSesson = createSession(inj, remoteAddress, gnutellaConfig);
    NetworkIdentity remoteIdentity = pingSesson.getIdentity();

    initializeMe(inj, 5, 1001);

    remoteIdentity.enterScope();
    pingSesson.enterScope();
    PingModule module = inj.getInstance(PingModule.class);

    final byte[] guid = GUID.generateGuid();
    // hops = 0, ttl = 3 which means we're not a crawler
    MessageHeader header = new MessageHeader(guid, MessageHeader.F_PING, (byte) 3);
    PingBody ping = new PingBody(null);

    module.messageReceived(new MessageReceivedEvent(null, new GnutellaMessage(header, ping)));

    pingSesson.exitScope();
    remoteIdentity.exitScope();

    // TODO when we populate our ping with ggep, control what that is and check for it here
    for (SessionModel session : sessions) {
      verify(writer).write(eq(session.getIdentity()), argThat(new ArgumentMatcher<Object>() {
        @Override
        public boolean matches(Object argument) {
          GnutellaMessage message = (GnutellaMessage) argument;
          MessageHeader header = message.getHeader();
          assertFalse("Guid of broadcast message cannot match input ping guid.",
              Arrays.equals(header.getGuid(), guid));
          byte[] nguid = header.getGuid();

          return header.equals(new MessageHeader(nguid, MessageHeader.F_PING, (byte) maxTtl,
              (byte) 0, MessageHeader.UNKNOWN_PAYLOAD_LENGTH));
        }
      }));
    }
  }

  @Test
  public void testMultiplexedRegularPing() {
    final ProtocolMessageWriter writer = mock(ProtocolMessageWriter.class);
    final SessionManagerImpl sessionManager = mock(SessionManagerImpl.class);
    final SlotsController slots = mock(SlotsController.class);
    when(slots.canAcceptNewConnection()).thenReturn(false);
    // so we're set up to not accept any new connections

    final int maxTtl = 5;
    final int maxPongs = 20;
    Injector inj = getInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(ProtocolMessageWriter.class).toInstance(writer);
        bind(SessionManagerImpl.class).toInstance(sessionManager);
        bind(SlotsController.class).toInstance(slots);
        bindConstant().annotatedWith(MaxTTL.class).to(maxTtl);
        bindConstant().annotatedWith(MaxPongsSent.class).to(maxPongs);
      }
    });

    // no sessions
    ProtocolConfig gnutellaConfig = getGnutellaProtocolConfig(inj);
    Set<SessionModel> sessions = Sets.newHashSet();
    when(sessionManager.getCurrentSessions(any(Protocol.class))).thenReturn(sessions);


    SocketAddress remoteAddress = new InetSocketAddress(InetAddresses.forString("1.2.3.6"), 1613);
    SessionModel pingSesson = createSession(inj, remoteAddress, gnutellaConfig);
    NetworkIdentity remoteIdentity = pingSesson.getIdentity();

    initializeMe(inj, 5, 1001);

    remoteIdentity.enterScope();
    pingSesson.enterScope();
    PingModule module = inj.getInstance(PingModule.class);

    final byte[] guid = GUID.generateGuid();
    // check our multiplexing for the max ttl
    byte sendingTtl = (byte) (maxTtl - 1);
    MessageHeader header = new MessageHeader(guid, MessageHeader.F_PING, (byte) sendingTtl);
    PingBody ping = new PingBody(null);

    module.messageReceived(new MessageReceivedEvent(null, new GnutellaMessage(header, ping)));

    PingSessionModel pingModel = inj.getInstance(PingSessionModel.class);

    pingSesson.exitScope();
    remoteIdentity.exitScope();

    assertNotNull(pingModel);

    // we should have an even distribution up to sendingttl - 1, which should be zero after
    // why? so if they want 'k' ttl, then we assume first level is us, so they want
    // 'k - 1' node levels past us. so maxpongs/ (k - 1) is the number per level,
    // and we should have the distribution in levels 1 through k-1 inclusive
    assertEquals(0, pingModel.getNeeded()[maxTtl].get());
    int neededPerLevel = maxPongs / (sendingTtl - 1);
    for (int i = 1; i <= maxTtl; i++) {
      if (i < sendingTtl) {
        assertEquals("not in correct distribution on ttl=" + i, neededPerLevel,
            pingModel.getNeeded()[i].get());
      } else {
        assertEquals("should have no requests at ttl=" + i, 0, pingModel.getNeeded()[i].get());
      }
    }
  }

  @Test
  public void testDirectPingAndAcceptTime() {
    final ProtocolMessageWriter writer = mock(ProtocolMessageWriter.class);
    final SessionManagerImpl sessionManager = mock(SessionManagerImpl.class);
    final SlotsController slots = mock(SlotsController.class);
    final Clock clock = mock(Clock.class);
    when(slots.canAcceptNewConnection()).thenReturn(true);
    // so we can accept connections

    final int expireTime = 6000;
    final long clockTime = 3000;
    final long afterTime = clockTime + expireTime;
    Injector inj = getInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(ProtocolMessageWriter.class).toInstance(writer);
        bind(SessionManagerImpl.class).toInstance(sessionManager);
        bind(SlotsController.class).toInstance(slots);
        bind(Clock.class).toInstance(clock);
        bindConstant().annotatedWith(PongExpireTime.class).to(expireTime);
      }
    });


    when(clock.currentTimeMillis()).thenReturn(clockTime);

    // no sessions
    ProtocolConfig gnutellaConfig = getGnutellaProtocolConfig(inj);
    Set<SessionModel> sessions = Sets.newHashSet();
    when(sessionManager.getCurrentSessions(any(Protocol.class))).thenReturn(sessions);


    SocketAddress remoteAddress = new InetSocketAddress(InetAddresses.forString("1.2.3.6"), 1613);
    SessionModel pingSesson = createSession(inj, remoteAddress, gnutellaConfig);
    NetworkIdentity remoteIdentity = pingSesson.getIdentity();

    InetSocketAddress address = createAddress("1.54.2.3", 8182);
    initializeMe(inj, address, 5, 1001);

    remoteIdentity.enterScope();
    pingSesson.enterScope();
    PingSessionModel pingModel = inj.getInstance(PingSessionModel.class);
    pingModel.setAcceptTime(0);

    PingModule module = inj.getInstance(PingModule.class);

    final byte[] guid = GUID.generateGuid();
    // send a direct ping, with hops = 0 and ttl = 1
    MessageHeader header = new MessageHeader(guid, MessageHeader.F_PING, (byte) 1, (byte) 0);
    PingBody ping = new PingBody(null);

    module.messageReceived(new MessageReceivedEvent(null, new GnutellaMessage(header, ping)));

    pingSesson.exitScope();
    remoteIdentity.exitScope();

    assertEquals(afterTime, pingModel.getAcceptTime());

    // TODO add ggep checking when we populate it
    MessageHeader returnHeader =
        new MessageHeader(guid, MessageHeader.F_PING_REPLY, (byte) 1, (byte) 0);
    PongBody returnBody = new PongBody(address, 5, 1001, new GGEP());

    verify(writer).write(eq(new GnutellaMessage(returnHeader, returnBody)));
  }

  @Test
  public void testNoDirectPingWhenNoSlots() {
    final ProtocolMessageWriter writer = mock(ProtocolMessageWriter.class);
    final SessionManagerImpl sessionManager = mock(SessionManagerImpl.class);
    final SlotsController slots = mock(SlotsController.class);
    when(slots.canAcceptNewConnection()).thenReturn(false);
    // so we CANNOT accept connections

    Injector inj = getInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(ProtocolMessageWriter.class).toInstance(writer);
        bind(SessionManagerImpl.class).toInstance(sessionManager);
        bind(SlotsController.class).toInstance(slots);
      }
    });

    // no sessions
    ProtocolConfig gnutellaConfig = getGnutellaProtocolConfig(inj);
    Set<SessionModel> sessions = Sets.newHashSet();
    when(sessionManager.getCurrentSessions(any(Protocol.class))).thenReturn(sessions);


    SocketAddress remoteAddress = new InetSocketAddress(InetAddresses.forString("1.2.3.6"), 1613);
    SessionModel pingSesson = createSession(inj, remoteAddress, gnutellaConfig);
    NetworkIdentity remoteIdentity = pingSesson.getIdentity();

    SocketAddress address = createAddress("1.54.2.3", 8182);
    initializeMe(inj, address, 5, 1001);

    remoteIdentity.enterScope();
    pingSesson.enterScope();
    PingModule module = inj.getInstance(PingModule.class);

    final byte[] guid = GUID.generateGuid();
    // send a direct ping, with hops = 0 and ttl = 1
    MessageHeader header = new MessageHeader(guid, MessageHeader.F_PING, (byte) 1, (byte) 0);
    PingBody ping = new PingBody(null);

    module.messageReceived(new MessageReceivedEvent(null, new GnutellaMessage(header, ping)));

    pingSesson.exitScope();
    remoteIdentity.exitScope();

    verify(writer, never()).write(any(GnutellaMessage.class));
  }

  @Test
  public void testCrawlerPing() {
    final ProtocolMessageWriter writer = mock(ProtocolMessageWriter.class);
    final SessionManagerImpl sessionManager = mock(SessionManagerImpl.class);
    final SlotsController slots = mock(SlotsController.class);
    when(slots.canAcceptNewConnection()).thenReturn(true);
    // so we can accept connections

    Injector inj = getInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(ProtocolMessageWriter.class).toInstance(writer);
        bind(SessionManagerImpl.class).toInstance(sessionManager);
        bind(SlotsController.class).toInstance(slots);
      }
    });

    // no sessions
    ProtocolConfig gnutellaConfig = getGnutellaProtocolConfig(inj);
    Protocol gnutella = gnutellaConfig.get();

    Set<SessionModel> sessions = Sets.newHashSet();
    when(sessionManager.getCurrentSessions(any(Protocol.class))).thenReturn(sessions);

    NetworkIdentityManager manager = inj.getInstance(NetworkIdentityManager.class);
    IdentityTagManager tags = inj.getInstance(IdentityTagManager.class);
    int num = 5;

    NetworkIdentity[] identities = new NetworkIdentity[num];
    for (int i = 0; i < identities.length; i++) {
      SocketAddress remoteAddress =
          new InetSocketAddress(InetAddresses.forString("1.2.3." + i), i + 100);
      int fileCount = i;
      int fileSize = i + 1;
      identities[i] = manager.createNetworkIdentity();
      manager.setSendingAddress(identities[i], gnutella, remoteAddress);
      manager.setListeningAddress(identities[i], gnutella, remoteAddress);
      identities[i].enterScope();
      GnutellaServantModel identityModel = inj.getInstance(GnutellaServantModel.class);
      identities[i].exitScope();
      identityModel.setFileCount(fileCount);
      identityModel.setFileSizeInKB(fileSize);
      manager.tagIdentity(tags.getLeafKey(), identities[i]);
    }

    // use the last one
    int using = num - 1;
    NetworkIdentity remoteIdentity = identities[using];
    SessionModel session = createSession(remoteIdentity, inj, gnutellaConfig);

    InetSocketAddress address = new InetSocketAddress(InetAddresses.forString("1.2.3.4"), 1123);
    initializeMe(inj, address, 5, 1001);

    remoteIdentity.enterScope();
    session.enterScope();
    PingModule module = inj.getInstance(PingModule.class);

    byte[] guid = new GUID().getBytes();

    // crawler ping, hops = 0, ttl = 2
    MessageHeader header = new MessageHeader(guid, MessageHeader.F_PING, (byte) 2, (byte) 0);
    PingBody body = new PingBody(null);
    module.messageReceived(new MessageReceivedEvent(null, new GnutellaMessage(header, body)));
    session.exitScope();
    remoteIdentity.exitScope();

    // we should NOT be responding with our own pong with this call
    MessageHeader returnHeader =
        new MessageHeader(guid, MessageHeader.F_PING_REPLY, (byte) 1, (byte) 0);
    PongBody returnBody = new PongBody(address, 5, 1001, new GGEP());
    verify(writer, never()).write(eq(new GnutellaMessage(returnHeader, returnBody)));

    // we should NOT be setting up any demultiplexing tables, not testing for this though

    returnHeader = new MessageHeader(guid, MessageHeader.F_PING_REPLY, (byte) 1, (byte) 1);
    for (int i = 0; i < identities.length; i++) {
      InetSocketAddress remoteAddress =
          new InetSocketAddress(InetAddresses.forString("1.2.3." + i), i + 100);
      int fileCount = i;
      int fileSize = i + 1;
      returnBody = new PongBody(remoteAddress, fileCount, fileSize, null);
      if (i == using) {
        verify(writer, never()).write(new GnutellaMessage(returnHeader, returnBody));
      } else {
        verify(writer).write(new GnutellaMessage(returnHeader, returnBody));
      }
    }
  }

  @Test
  public void testCacheUsedAndNeededDecremented() {
    final ProtocolMessageWriter writer = mock(ProtocolMessageWriter.class);
    final SessionManagerImpl sessionManager = mock(SessionManagerImpl.class);
    final SlotsController slots = mock(SlotsController.class);
    final Clock clock = mock(Clock.class);

    when(slots.canAcceptNewConnection()).thenReturn(false);
    // so we CANNOT accept connections

    final int pongExpireTime = 1000;
    final int maxTtl = 6;
    final int maxPongs = 15;
    Injector inj = getInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(ProtocolMessageWriter.class).toInstance(writer);
        bind(SessionManagerImpl.class).toInstance(sessionManager);
        bind(SlotsController.class).toInstance(slots);
        bind(Clock.class).toInstance(clock);
        bindConstant().annotatedWith(PongExpireTime.class).to(pongExpireTime);
        bindConstant().annotatedWith(MaxTTL.class).to(maxTtl);
        bindConstant().annotatedWith(MaxPongsSent.class).to(maxPongs);
      }
    });
    // so we don't expire the cache
    when(clock.currentTimeMillis()).thenReturn(500l);

    // no sessions
    ProtocolConfig gnutellaConfig = getGnutellaProtocolConfig(inj);
    when(sessionManager.getCurrentSessions(any(Protocol.class))).thenReturn(
        Sets.<SessionModel>newHashSet());


    InetSocketAddress remoteAddress = new InetSocketAddress(InetAddresses.forString("5.5.5.5"), 1613);
    SessionModel pingSesson = createSession(inj, remoteAddress, gnutellaConfig);
    NetworkIdentity remoteIdentity = pingSesson.getIdentity();

    // to quickly overview this:
    // we are putting pongs in the cache, and sometimes using the requesting identity (when j == 1)
    // later, we check to make sure that
    // 1. we have used only the pongs in the cache that we 'needed'
    // 2. we ignore pongs that come from the requesting identity
    // 3. when we don't have enough pongs in the cache, needed array is populated correctly

    AdvancedPongCache cache = inj.getInstance(AdvancedPongCache.class);
    Map<Integer, List<SessionModel>> ttlToSessions = Maps.newHashMap();
    for (int ttlNum = 1; ttlNum < maxTtl; ttlNum++) {
      List<SessionModel> sessions = Lists.newArrayList();
      ttlToSessions.put(ttlNum, sessions);
      for (int j = 1; j <= ttlNum; j++) {
        SessionModel session;
        InetSocketAddress address;
        if (j == 1) {
          address = remoteAddress;
          session = pingSesson;
        } else {
          address = createAddress("1.55.5." + j * ttlNum, j + 100);
          session = createSession(inj, address, gnutellaConfig);
        }
        sessions.add(session);
        GGEP ggep = new GGEP();
        ggep.put("test", j);
        PongBody pong = new PongBody(address, j, j * ttlNum + 1, ggep);
        cache.getCache()[ttlNum].add(new AdvancedPongCache.CacheEntry(pong, session.getIdentity()));
      }
    }


    SocketAddress address = createAddress("127.0.0.1", 8182);
    initializeMe(inj, address, 5, 1001);

    remoteIdentity.enterScope();
    pingSesson.enterScope();
    PingModule module = inj.getInstance(PingModule.class);

    final byte[] guid = GUID.generateGuid();
    // send a direct ping, with hops = 0 and ttl = 1
    MessageHeader header = new MessageHeader(guid, MessageHeader.F_PING, (byte) maxTtl, (byte) 0);
    PingBody ping = new PingBody(null);

    module.messageReceived(new MessageReceivedEvent(null, new GnutellaMessage(header, ping)));

    PingSessionModel pingSession = inj.getInstance(PingSessionModel.class);
    pingSesson.exitScope();
    remoteIdentity.exitScope();

    int pongsPerGroup = maxPongs / (header.getTtl() - 1);

    for (int ttlNum : ttlToSessions.keySet()) {
      MessageHeader responseHeader =
          new MessageHeader(header.getGuid(), MessageHeader.F_PING_REPLY, (byte) 1, (byte) (ttlNum));
      int needed = pongsPerGroup;
      List<SessionModel> sessions = ttlToSessions.get(ttlNum);

      int sent = 0;
      for (int j = 1; j <= maxTtl; j++) {

        InetSocketAddress pongAddress;
        if (j == 1) {
          pongAddress = remoteAddress;
        } else {
          pongAddress = createAddress("1.55.5." + j * ttlNum, j + 100);
        }
        GGEP ggep = new GGEP();
        ggep.put("test", j);
        PongBody pong = new PongBody(pongAddress, j, j * ttlNum + 1, ggep);

        GnutellaMessage message = new GnutellaMessage(responseHeader, pong);
        if (j <= sessions.size() && sent < pongsPerGroup
            && sessions.get(j - 1).getIdentity() != remoteIdentity) {
          verify(writer).write(eq(message));
          needed--;
          sent++;
        } else {
          verify(writer, never()).write(eq(message));
        }
      }
      assertEquals(needed, pingSession.getNeeded()[ttlNum].get());
    }
  }

  @Test
  public void testAddPongToCache() {
    final ProtocolMessageWriter writer = mock(ProtocolMessageWriter.class);
    final SessionManagerImpl sessionManager = mock(SessionManagerImpl.class);

    Injector inj = getInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(ProtocolMessageWriter.class).toInstance(writer);
        bind(SessionManagerImpl.class).toInstance(sessionManager);
      }
    });

    // no sessions
    ProtocolConfig gnutellaConfig = getGnutellaProtocolConfig(inj);
    when(sessionManager.getCurrentSessions(any(Protocol.class))).thenReturn(
        Sets.<SessionModel>newHashSet());

    SocketAddress remoteAddress = new InetSocketAddress(InetAddresses.forString("5.5.5.5"), 1613);
    SessionModel pongSesson = createSession(inj, remoteAddress, gnutellaConfig);
    NetworkIdentity remoteIdentity = pongSesson.getIdentity();

    byte[] guid = GUID.generateGuid();
    MessageHeader header = new MessageHeader(guid, MessageHeader.F_PING_REPLY, (byte) 1, (byte) 3);
    PongBody pongBody = new PongBody(createAddress("2.43.3.1", 4), 3, 32, null);

    remoteIdentity.enterScope();
    pongSesson.enterScope();
    PingModule module = inj.getInstance(PingModule.class);
    module.messageReceived(new MessageReceivedEvent(null, new GnutellaMessage(header, pongBody)));
    pongSesson.exitScope();
    remoteIdentity.exitScope();

    AdvancedPongCache cache = inj.getInstance(AdvancedPongCache.class);
    CacheEntry entry = cache.getCache()[header.getHops() + 1].get(0);
    assertEquals(remoteIdentity, entry.getIdentity());
    assertEquals(pongBody, entry.getBody());
  }

  @Test
  public void testPongDemultiplex() {
    final ProtocolMessageWriter writer = mock(ProtocolMessageWriter.class);
    final SessionManagerImpl sessionManager = mock(SessionManagerImpl.class);

    Injector inj = getInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(ProtocolMessageWriter.class).toInstance(writer);
        bind(SessionManagerImpl.class).toInstance(sessionManager);
      }
    });
    ProtocolConfig gnutellaConfig = getGnutellaProtocolConfig(inj);

    byte targetHops = 3;
    int wanted = 2;
    byte[] askGuid = GUID.generateGuid();

    SessionModel activeSession = createSession(inj, createAddress("1.2.3.4", 10), gnutellaConfig);
    NetworkIdentity activeSessionIdentity = activeSession.getIdentity();

    activeSessionIdentity.enterScope();
    activeSession.enterScope();
    PingSessionModel sessionModel = inj.getInstance(PingSessionModel.class);
    sessionModel.getNeeded()[targetHops + 1].set(wanted);
    sessionModel.setAcceptGuid(askGuid);
    activeSession.exitScope();
    activeSessionIdentity.exitScope();

    Set<SessionModel> sessions = Sets.newHashSet(activeSession);
    when(sessionManager.getCurrentSessions(any(Protocol.class))).thenReturn(sessions);

    SocketAddress remoteAddress = new InetSocketAddress(InetAddresses.forString("5.5.5.5"), 1613);
    SessionModel pongSesson = createSession(inj, remoteAddress, gnutellaConfig);
    NetworkIdentity remoteIdentity = pongSesson.getIdentity();

    byte[] guid = GUID.generateGuid();
    MessageHeader header =
        new MessageHeader(guid, MessageHeader.F_PING_REPLY, (byte) 1, (byte) targetHops);
    PongBody pongBody = new PongBody(createAddress("2.43.3.1", 4), 3, 32, null);

    remoteIdentity.enterScope();
    pongSesson.enterScope();
    PingModule module = inj.getInstance(PingModule.class);
    module.messageReceived(new MessageReceivedEvent(null, new GnutellaMessage(header, pongBody)));
    pongSesson.exitScope();
    remoteIdentity.exitScope();

    MessageHeader forwardedHeader =
        new MessageHeader(askGuid, MessageHeader.F_PING_REPLY, (byte) 1, (byte) (targetHops + 1));

    verify(writer).write(eq(activeSessionIdentity),
        eq(new GnutellaMessage(forwardedHeader, pongBody)),
        eq(ConnectionOptions.EXIT_IF_NO_CONNECTION), eq(HandshakeOptions.EXIT_IF_HANDSHAKING));
  }
}

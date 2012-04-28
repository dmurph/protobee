package edu.cornell.jnutella.gnutella.modules;

import static org.junit.Assert.*;
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
import java.util.Set;

import org.junit.Test;
import org.mockito.ArgumentMatcher;

import com.google.common.collect.Sets;
import com.google.common.net.InetAddresses;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;

import edu.cornell.jnutella.AbstractTest;
import edu.cornell.jnutella.extension.GGEP;
import edu.cornell.jnutella.gnutella.GnutellaServantModel;
import edu.cornell.jnutella.gnutella.SlotsController;
import edu.cornell.jnutella.gnutella.constants.MaxTTL;
import edu.cornell.jnutella.gnutella.messages.GnutellaMessage;
import edu.cornell.jnutella.gnutella.messages.MessageHeader;
import edu.cornell.jnutella.gnutella.messages.PingBody;
import edu.cornell.jnutella.gnutella.messages.PongBody;
import edu.cornell.jnutella.gnutella.modules.ping.MaxPongsSent;
import edu.cornell.jnutella.gnutella.modules.ping.PingModule;
import edu.cornell.jnutella.gnutella.modules.ping.PingSessionModel;
import edu.cornell.jnutella.gnutella.session.MessageReceivedEvent;
import edu.cornell.jnutella.identity.IdentityTagManager;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.identity.NetworkIdentityManager;
import edu.cornell.jnutella.network.ProtocolMessageWriter;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.ProtocolConfig;
import edu.cornell.jnutella.session.SessionManager;
import edu.cornell.jnutella.session.SessionModel;
import edu.cornell.jnutella.util.GUID;

public class PingModuleTest extends AbstractTest {

  @Test
  public void testBroadcastOnEmptyCache() {
    final ProtocolMessageWriter writer = mock(ProtocolMessageWriter.class);
    final SessionManager sessionManager = mock(SessionManager.class);
    final SlotsController slots = mock(SlotsController.class);
    when(slots.canAcceptNewConnection()).thenReturn(false);
    // so we're set up to not accept any new connections

    final int maxTtl = 5;
    Injector inj = getInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(ProtocolMessageWriter.class).toInstance(writer);
        bind(SessionManager.class).toInstance(sessionManager);
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
    final SessionManager sessionManager = mock(SessionManager.class);
    final SlotsController slots = mock(SlotsController.class);
    when(slots.canAcceptNewConnection()).thenReturn(false);
    // so we're set up to not accept any new connections

    final int maxTtl = 5;
    final int maxPongs = 20;
    Injector inj = getInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(ProtocolMessageWriter.class).toInstance(writer);
        bind(SessionManager.class).toInstance(sessionManager);
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
  public void testDirectPing() {
    final ProtocolMessageWriter writer = mock(ProtocolMessageWriter.class);
    final SessionManager sessionManager = mock(SessionManager.class);
    final SlotsController slots = mock(SlotsController.class);
    when(slots.canAcceptNewConnection()).thenReturn(true);
    // so we can accept connections

    Injector inj = getInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(ProtocolMessageWriter.class).toInstance(writer);
        bind(SessionManager.class).toInstance(sessionManager);
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


    // TODO add ggep checking when we populate it
    MessageHeader returnHeader =
        new MessageHeader(guid, MessageHeader.F_PING_REPLY, (byte) 1, (byte) 0);
    PongBody returnBody = new PongBody(address, 5, 1001, new GGEP());

    verify(writer).write(eq(new GnutellaMessage(returnHeader, returnBody)));
  }
  
  @Test
  public void testNoDirectPingWhenNoSlots() {
    final ProtocolMessageWriter writer = mock(ProtocolMessageWriter.class);
    final SessionManager sessionManager = mock(SessionManager.class);
    final SlotsController slots = mock(SlotsController.class);
    when(slots.canAcceptNewConnection()).thenReturn(false);
    // so we CANNOT accept connections

    Injector inj = getInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(ProtocolMessageWriter.class).toInstance(writer);
        bind(SessionManager.class).toInstance(sessionManager);
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
    final SessionManager sessionManager = mock(SessionManager.class);
    final SlotsController slots = mock(SlotsController.class);
    when(slots.canAcceptNewConnection()).thenReturn(true);
    // so we can accept connections

    Injector inj = getInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(ProtocolMessageWriter.class).toInstance(writer);
        bind(SessionManager.class).toInstance(sessionManager);
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
      manager.setNetworkAddress(identities[i], gnutella, remoteAddress);
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

    SocketAddress address = new InetSocketAddress(InetAddresses.forString("1.2.3.4"), 1123);
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
      SocketAddress remoteAddress =
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
    final SessionManager sessionManager = mock(SessionManager.class);
    final SlotsController slots = mock(SlotsController.class);
    when(slots.canAcceptNewConnection()).thenReturn(false);
    // so we CANNOT accept connections

    Injector inj = getInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(ProtocolMessageWriter.class).toInstance(writer);
        bind(SessionManager.class).toInstance(sessionManager);
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
}

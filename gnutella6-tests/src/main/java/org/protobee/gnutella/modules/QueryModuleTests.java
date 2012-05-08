package org.protobee.gnutella.modules;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Set;

import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.protobee.gnutella.AbstractGnutellaTest;
import org.protobee.gnutella.messages.GnutellaMessage;
import org.protobee.gnutella.messages.MessageHeader;
import org.protobee.gnutella.messages.QueryBody;
import org.protobee.gnutella.routing.managers.QueryRoutingTableManager;
import org.protobee.gnutella.session.MessageReceivedEvent;
import org.protobee.gnutella.util.GUID;
import org.protobee.identity.NetworkIdentity;
import org.protobee.network.ProtobeeMessageWriter.HandshakeOptions;
import org.protobee.network.ProtobeeMessageWriterImpl;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolConfig;
import org.protobee.session.SessionManagerImpl;
import org.protobee.session.SessionModel;

import com.google.common.collect.Sets;
import com.google.common.net.InetAddresses;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;


public class QueryModuleTests extends AbstractGnutellaTest {
  
  // TODO add sharedfileservice interface
  @Test
  public void testBasics() {
    final ProtobeeMessageWriterImpl writer = mock(ProtobeeMessageWriterImpl.class);
    final SessionManagerImpl sessionManager = mock(SessionManagerImpl.class);
    
    Injector inj = getInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(ProtobeeMessageWriterImpl.class).toInstance(writer);
        bind(SessionManagerImpl.class).toInstance(sessionManager);
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
    SessionModel querySesson = createSession(inj, remoteAddress, gnutellaConfig);
    NetworkIdentity remoteIdentity = querySesson.getIdentity();

    initializeMe(inj, 5, 1001);
    QueryRoutingTableManager qrtManager = inj.getInstance(QueryRoutingTableManager.class);

    remoteIdentity.enterScope();
    querySesson.enterScope();
    QueryModule module = inj.getInstance(QueryModule.class);

    final byte[] guid = GUID.generateGuid();
    MessageHeader header = new MessageHeader(guid, MessageHeader.F_QUERY, (byte) 3);
    QueryBody query = new QueryBody((short) 0, null, null, null);

    module.messageReceived(new MessageReceivedEvent(null, new GnutellaMessage(header, query)));

    querySesson.exitScope();
    remoteIdentity.exitScope();

    verify(writer, times(sessions.size())).write(argThat(new ArgumentMatcher<Object>() {
      @Override
      public boolean matches(Object argument) {
        GnutellaMessage message = (GnutellaMessage) argument;
        MessageHeader header = message.getHeader();
        byte[] nguid = header.getGuid();

        return header.equals(new MessageHeader(nguid, MessageHeader.F_QUERY, (byte) 2,
            (byte) 1, MessageHeader.UNKNOWN_PAYLOAD_LENGTH));
      }
    }), eq(HandshakeOptions.WAIT_FOR_HANDSHAKE));
    
    assertEquals(qrtManager.isRoutedForQuerys(guid), true);
  
  }
  
}


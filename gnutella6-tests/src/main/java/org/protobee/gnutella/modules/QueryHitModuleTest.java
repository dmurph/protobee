package org.protobee.gnutella.modules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.protobee.gnutella.AbstractGnutellaTest;
import org.protobee.gnutella.messages.GnutellaMessage;
import org.protobee.gnutella.messages.MessageHeader;
import org.protobee.gnutella.messages.QueryHitBody;
import org.protobee.gnutella.routing.IdentityHash;
import org.protobee.gnutella.routing.managers.PushRoutingTableManager;
import org.protobee.gnutella.routing.managers.QueryRoutingTableManager;
import org.protobee.gnutella.session.MessageReceivedEvent;
import org.protobee.gnutella.util.GUID;
import org.protobee.identity.NetworkIdentity;
import org.protobee.network.ProtobeeMessageWriter.HandshakeOptions;
import org.protobee.network.ProtobeeMessageWriterImpl;
import org.protobee.protocol.ProtocolConfig;
import org.protobee.session.SessionModel;
import org.protobee.stats.DropLog;

import com.google.common.net.InetAddresses;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;


public class QueryHitModuleTest extends AbstractGnutellaTest {
  
  @Test
  public void testNoRoutingDrop() {
    final ProtobeeMessageWriterImpl writer = mock(ProtobeeMessageWriterImpl.class);
    final DropLog dropLog = mock(DropLog.class);
    
    Injector inj = getInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(ProtobeeMessageWriterImpl.class).toInstance(writer);
        bind(DropLog.class).toInstance(dropLog);
      }
    });

    ProtocolConfig gnutellaConfig = getGnutellaProtocolConfig(inj);

    SocketAddress remoteAddress = new InetSocketAddress(InetAddresses.forString("1.2.3.6"), 1613);
    SessionModel querySesson = createSession(inj, remoteAddress, gnutellaConfig);
    NetworkIdentity remoteIdentity = querySesson.getIdentity();

    initializeMe(inj, 5, 1001);
    QueryRoutingTableManager qrtManager = inj.getInstance(QueryRoutingTableManager.class);
    
    remoteIdentity.enterScope();
    querySesson.enterScope();
    QueryHitModule module = inj.getInstance(QueryHitModule.class);

    final byte[] guid = GUID.generateGuid();
    MessageHeader header = new MessageHeader(guid, MessageHeader.F_QUERY_REPLY, (byte) 3);
    QueryHitBody queryHit = mock(QueryHitBody.class);
    GnutellaMessage message = new GnutellaMessage(header, queryHit);
    
    module.messageReceived(new MessageReceivedEvent(null, message));

    querySesson.exitScope();
    remoteIdentity.exitScope();

    assertEquals(qrtManager.isRoutedForQuerys(guid), false);
    
    verify(dropLog).messageDropped(eq(remoteAddress), eq(gnutellaConfig.get()), eq(message),
      eq("Query hit dropped - no routing found for query guid"));
  }
  
  @Test
  public void testBasics() {
    final ProtobeeMessageWriterImpl writer = mock(ProtobeeMessageWriterImpl.class);
    final DropLog dropLog = mock(DropLog.class);
    
    Injector inj = getInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(ProtobeeMessageWriterImpl.class).toInstance(writer);
        bind(DropLog.class).toInstance(dropLog);
      }
    });

    ProtocolConfig gnutellaConfig = getGnutellaProtocolConfig(inj);

    SocketAddress remoteAddress = new InetSocketAddress(InetAddresses.forString("1.2.3.6"), 1613);
    SessionModel querySesson = createSession(inj, remoteAddress, gnutellaConfig);
    NetworkIdentity remoteIdentity = querySesson.getIdentity();

    initializeMe(inj, 5, 1001);
    QueryRoutingTableManager qrtManager = inj.getInstance(QueryRoutingTableManager.class);
    PushRoutingTableManager pushRTManager = inj.getInstance(PushRoutingTableManager.class);

    remoteIdentity.enterScope();
    querySesson.enterScope();
    QueryHitModule module = inj.getInstance(QueryHitModule.class);
    
    final byte[] guid = GUID.generateGuid();
    qrtManager.addRouting(guid, remoteIdentity);
    
    MessageHeader header = new MessageHeader(guid, MessageHeader.F_QUERY_REPLY, (byte) 3);
    QueryHitBody queryHit = mock(QueryHitBody.class);
    GnutellaMessage message = new GnutellaMessage(header, queryHit);
    
    module.messageReceived(new MessageReceivedEvent(null, message));

    querySesson.exitScope();
    remoteIdentity.exitScope();

    // check push is called
    assertEquals(pushRTManager.isRouted(guid), false);
    
    // check hash is in placed
    IdentityHash queryHash = new IdentityHash(header.getGuid(), queryHit.getUrns());
    assertTrue(qrtManager.hasQueryHit(queryHash));
    
    // check writer was called
    verify(writer, times(1)).write(argThat(new ArgumentMatcher<Object>() {
      @Override
      public boolean matches(Object argument) {
        GnutellaMessage message = (GnutellaMessage) argument;
        MessageHeader header = message.getHeader();
        byte[] nguid = header.getGuid();

        return header.equals(new MessageHeader(nguid, MessageHeader.F_QUERY, (byte) 2,
            (byte) 1, MessageHeader.UNKNOWN_PAYLOAD_LENGTH));
      }
    }), eq(HandshakeOptions.WAIT_FOR_HANDSHAKE));
    
  }
  
  @Test
  public void dropTooManyRoutes() {
    final ProtobeeMessageWriterImpl writer = mock(ProtobeeMessageWriterImpl.class);
    final DropLog dropLog = mock(DropLog.class);
    
    Injector inj = getInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(ProtobeeMessageWriterImpl.class).toInstance(writer);
        bind(DropLog.class).toInstance(dropLog);
      }
    });

    ProtocolConfig gnutellaConfig = getGnutellaProtocolConfig(inj);

    SocketAddress remoteAddress = new InetSocketAddress(InetAddresses.forString("1.2.3.6"), 1613);
    SessionModel querySesson = createSession(inj, remoteAddress, gnutellaConfig);
    NetworkIdentity remoteIdentity = querySesson.getIdentity();

    initializeMe(inj, 5, 1001);
    QueryRoutingTableManager qrtManager = inj.getInstance(QueryRoutingTableManager.class);
    
    remoteIdentity.enterScope();
    querySesson.enterScope();
    QueryHitModule module = inj.getInstance(QueryHitModule.class);
    
    final byte[] guid = GUID.generateGuid();
    qrtManager.addRouting(guid, remoteIdentity);
    
    MessageHeader header = new MessageHeader(guid, MessageHeader.F_QUERY_REPLY, (byte) 3);
    QueryHitBody queryHit = mock(QueryHitBody.class);
    when(queryHit.getNumHits()).thenReturn((byte) 210);
    GnutellaMessage message = new GnutellaMessage(header, queryHit);
    
    // too many 
    module.messageReceived(new MessageReceivedEvent(null, message));

    querySesson.exitScope();
    remoteIdentity.exitScope();

    // check hash is in placed
    IdentityHash queryHash = new IdentityHash(header.getGuid(), queryHit.getUrns());
    assertTrue(qrtManager.hasQueryHit(queryHash));
    
    // verify message dropped due to too high num of query hits routed
    verify(dropLog).messageDropped(eq(remoteAddress), eq(gnutellaConfig.get()), eq(message),
      eq("Query Hit not routed - routed result count to this node exceeds maximum"));
    
  }
  
}


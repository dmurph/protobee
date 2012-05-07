package org.protobee.gnutella.modules;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.junit.Test;
import org.protobee.gnutella.AbstractGnutellaTest;
import org.protobee.gnutella.messages.GnutellaMessage;
import org.protobee.gnutella.messages.MessageHeader;
import org.protobee.gnutella.messages.QueryHitBody;
import org.protobee.gnutella.routing.managers.QueryRoutingTableManager;
import org.protobee.gnutella.session.MessageReceivedEvent;
import org.protobee.gnutella.util.GUID;
import org.protobee.identity.NetworkIdentity;
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
    QueryHitBody queryHit = new QueryHitBody();
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
//    final ProtobeeMessageWriterImpl writer = mock(ProtobeeMessageWriterImpl.class);
//    final DropLog dropLog = mock(DropLog.class);
//    final QueryHitBody qhBody = mock(QueryHitBody.class);
//    
//    Injector inj = getInjector(new AbstractModule() {
//      @Override
//      protected void configure() {
//        bind(ProtobeeMessageWriterImpl.class).toInstance(writer);
//        bind(DropLog.class).toInstance(dropLog);
//      }
//    });
//
//    ProtocolConfig gnutellaConfig = getGnutellaProtocolConfig(inj);
//
//    SocketAddress remoteAddress = new InetSocketAddress(InetAddresses.forString("1.2.3.6"), 1613);
//    SessionModel querySesson = createSession(inj, remoteAddress, gnutellaConfig);
//    NetworkIdentity remoteIdentity = querySesson.getIdentity();
//
//    initializeMe(inj, 5, 1001);
//    QueryRoutingTableManager qrtManager = inj.getInstance(QueryRoutingTableManager.class);
//    
//    remoteIdentity.enterScope();
//    querySesson.enterScope();
//    QueryHitModule module = inj.getInstance(QueryHitModule.class);
//
//    final byte[] guid = GUID.generateGuid();
//    qrtManager.addRouting(guid, remoteIdentity);
//    
//    MessageHeader header = new MessageHeader(guid, MessageHeader.F_QUERY_REPLY, (byte) 3);
//    QueryHitBody queryHit = new QueryHitBody();
//    GnutellaMessage message = new GnutellaMessage(header, queryHit);
//    
//    module.messageReceived(new MessageReceivedEvent(null, message));
//
//    querySesson.exitScope();
//    remoteIdentity.exitScope();
//
//    assertEquals(qrtManager.isRoutedForQuerys(guid), false);
//    
//    verify(dropLog).messageDropped(eq(remoteAddress), eq(gnutellaConfig.get()), eq(message),
//      eq("Query hit dropped - no routing found for query guid"));
//    
  }
  
}


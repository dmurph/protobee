package edu.cornell.jnutella.gnutella.modules;

import static org.mockito.Mockito.*;

import java.io.ObjectInputStream.GetField;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.junit.Test;

import com.google.common.net.InetAddresses;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;

import edu.cornell.jnutella.AbstractTest;
import edu.cornell.jnutella.extension.GGEP;
import edu.cornell.jnutella.gnutella.GnutellaIdentityModel;
import edu.cornell.jnutella.gnutella.RequestFilter;
import edu.cornell.jnutella.gnutella.messages.GnutellaMessage;
import edu.cornell.jnutella.gnutella.messages.MessageHeader;
import edu.cornell.jnutella.gnutella.messages.PingBody;
import edu.cornell.jnutella.gnutella.messages.PongBody;
import edu.cornell.jnutella.gnutella.modules.ping.PingModule;
import edu.cornell.jnutella.gnutella.session.MessageReceivedEvent;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.identity.NetworkIdentityManager;
import edu.cornell.jnutella.network.ProtocolMessageWriter;
import edu.cornell.jnutella.session.SessionModel;
import edu.cornell.jnutella.util.GUID;

public class PingModuleTest extends AbstractTest {

  @Test
  public void testFilter() {
    Injector inj = getInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(RequestFilter.class).toInstance(mock(RequestFilter.class));
      }
    });


  }

  @Test
  public void testDirectPing() {
    final ProtocolMessageWriter writer = mock(ProtocolMessageWriter.class);

    Injector inj = getInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(ProtocolMessageWriter.class).toInstance(writer);
      }
    });
    SocketAddress remoteAddress = new InetSocketAddress(InetAddresses.forString("1.2.3.6"), 1613);
    SessionModel pingSesson = createSession(inj, remoteAddress, getGnutellaProtocolConfig(inj));
    NetworkIdentity remoteIdentity = pingSesson.getIdentity();

    NetworkIdentityManager manager = inj.getInstance(NetworkIdentityManager.class);
    GnutellaIdentityModel me =
        (GnutellaIdentityModel) manager.getMe().getModel(getGnutellaProtocol(inj));
    me.setFileCount(5);
    me.setFileSizeInKB(1001);
    SocketAddress address = new InetSocketAddress(InetAddresses.forString("1.2.3.4"), 1123);
    me.setNetworkAddress(address);

    remoteIdentity.enterScope();
    pingSesson.enterScope();
    PingModule module = inj.getInstance(PingModule.class);
    pingSesson.exitScope();
    remoteIdentity.exitScope();

    byte[] guid = new GUID().getBytes();

    MessageHeader header = new MessageHeader(guid, MessageHeader.F_PING, (byte) 1, (byte) 0);
    PingBody body = new PingBody(null);
    module.messageReceived(new MessageReceivedEvent(null, new GnutellaMessage(header, body)));


    // TODO add ggep checking when we populate it
    MessageHeader returnHeader =
        new MessageHeader(guid, MessageHeader.F_PING_REPLY, (byte) 1, (byte) 0);
    PongBody returnBody = new PongBody(address, 5, 1001, new GGEP());
    
    verify(writer).write(eq(new GnutellaMessage(returnHeader, returnBody)));
  }
  
  @Test
  public void testCachedPongs() {
    final ProtocolMessageWriter writer = mock(ProtocolMessageWriter.class);

    Injector inj = getInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(ProtocolMessageWriter.class).toInstance(writer);
      }
    });
    SocketAddress remoteAddress = new InetSocketAddress(InetAddresses.forString("1.2.3.6"), 1613);
    SessionModel pingSesson = createSession(inj, remoteAddress, getGnutellaProtocolConfig(inj));
    NetworkIdentity remoteIdentity = pingSesson.getIdentity();

    NetworkIdentityManager manager = inj.getInstance(NetworkIdentityManager.class);
    GnutellaIdentityModel me =
        (GnutellaIdentityModel) manager.getMe().getModel(getGnutellaProtocol(inj));
    me.setFileCount(5);
    me.setFileSizeInKB(1001);
    SocketAddress address = new InetSocketAddress(InetAddresses.forString("1.2.3.4"), 1123);
    me.setNetworkAddress(address);

    remoteIdentity.enterScope();
    pingSesson.enterScope();
    PingModule module = inj.getInstance(PingModule.class);
    pingSesson.exitScope();
    remoteIdentity.exitScope();

    byte[] guid = new GUID().getBytes();

    MessageHeader header = new MessageHeader(guid, MessageHeader.F_PING, (byte) 1, (byte) 0);
    PingBody body = new PingBody(null);
    module.messageReceived(new MessageReceivedEvent(null, new GnutellaMessage(header, body)));


    // TODO add ggep checking when we populate it
    MessageHeader returnHeader =
        new MessageHeader(guid, MessageHeader.F_PING_REPLY, (byte) 1, (byte) 0);
    PongBody returnBody = new PongBody(address, 5, 1001, new GGEP());
    
    verify(writer).write(eq(new GnutellaMessage(returnHeader, returnBody)));
  }
  
  @Test
  public void testForwading() {
    
  }
}

package org.protobee.examples.broadcast;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.local.LocalAddress;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.junit.Test;
import org.protobee.JnutellaServantBootstrapper;
import org.protobee.ProtobeeGuiceModule;
import org.protobee.netty.LocalNettyTester;
import org.protobee.network.ConnectionCreator;
import org.protobee.protocol.ProtocolConfig;
import org.protobee.protocol.ProtocolModel;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class BroadcastHandshakeTest extends AbstractBroadcastTest {
  @Test
  public void testHandshake() throws Exception {
    Injector inj = Guice.createInjector(new ProtobeeGuiceModule(), new BroadcastGuiceModule());

    JnutellaServantBootstrapper bootstrap = inj.getInstance(JnutellaServantBootstrapper.class);
    bootstrap.startup();

    LocalNettyTester tester = createLocalNettyTester();
    tester.connect(new LocalAddress("broadcast-example"), new LocalAddress("test"));
    basicHandshake(tester);
    tester.verifyNotClosed();

    bootstrap.shutdown(1000);
  }
  
  @Test
  public void testSendingHandshakeNoTimed() throws Exception {
    Injector inj = Guice.createInjector(new ProtobeeGuiceModule(), new BroadcastGuiceModule());

    JnutellaServantBootstrapper bootstrap = inj.getInstance(JnutellaServantBootstrapper.class);
    bootstrap.startup();
    

    LocalNettyTester tester = createLocalNettyTester();
    tester.bind(new LocalAddress("test"));
    

    ProtocolConfig broadcastConfig = getConfig(inj, Broadcast.class);
    ProtocolModel model = fromConfig(inj, broadcastConfig);
    
    ConnectionCreator creator = inj.getInstance(ConnectionCreator.class);
    creator.connect(model, new LocalAddress("test"), HttpMethod.valueOf("SAY"), "/");
    
    tester.verifyReceived(ChannelBuffers.wrappedBuffer(timedHandshake0.getBytes()));
    tester.writeAndWait(ChannelBuffers.wrappedBuffer(handshake1.getBytes()), 1000);
    tester.verifyReceived(ChannelBuffers.wrappedBuffer(handshake2.getBytes()));
    
    tester.verifyNotClosed();

    bootstrap.shutdown(1000);
  }
  
  @Test
  public void testSendingHandshakeTimed() throws Exception {
    Injector inj = Guice.createInjector(new ProtobeeGuiceModule(), new BroadcastGuiceModule());

    JnutellaServantBootstrapper bootstrap = inj.getInstance(JnutellaServantBootstrapper.class);
    bootstrap.startup();
    

    LocalNettyTester tester = createLocalNettyTester();
    tester.bind(new LocalAddress("test"));
    

    ProtocolConfig broadcastConfig = getConfig(inj, Broadcast.class);
    ProtocolModel model = fromConfig(inj, broadcastConfig);
    
    ConnectionCreator creator = inj.getInstance(ConnectionCreator.class);
    creator.connect(model, new LocalAddress("test"), HttpMethod.valueOf("SAY"), "/");
    
    tester.verifyReceived(ChannelBuffers.wrappedBuffer(timedHandshake0.getBytes()));
    tester.writeAndWait(ChannelBuffers.wrappedBuffer(timedHandshake1.getBytes()), 1000);
    tester.verifyReceived(ChannelBuffers.wrappedBuffer(timedHandshake2.getBytes()));
    
    tester.verifyNotClosed();

    bootstrap.shutdown(1000);
  }
  
  @Test
  public void testTimedHandshake() throws Exception {
    Injector inj = Guice.createInjector(new ProtobeeGuiceModule(), new BroadcastGuiceModule());

    JnutellaServantBootstrapper bootstrap = inj.getInstance(JnutellaServantBootstrapper.class);
    bootstrap.startup();

    LocalNettyTester tester = createLocalNettyTester();
    tester.connect(new LocalAddress("broadcast-example"), new LocalAddress("test"));
    timedHandshake(tester);
    tester.verifyNotClosed();

    bootstrap.shutdown(1000);
  }
}

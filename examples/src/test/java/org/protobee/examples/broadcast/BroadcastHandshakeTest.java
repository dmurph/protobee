package org.protobee.examples.broadcast;

import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.local.LocalAddress;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.junit.Test;
import org.protobee.ProtobeeGuiceModule;
import org.protobee.ProtobeeServantBootstrapper;
import org.protobee.examples.broadcast.modules.BroadcastMessageModule;
import org.protobee.guice.scopes.SessionScope;
import org.protobee.modules.ProtocolModule;
import org.protobee.netty.LocalNettyTester;
import org.protobee.network.ConnectionCreator;
import org.protobee.protocol.ProtocolConfig;
import org.protobee.protocol.ProtocolModel;

import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.util.Modules;

public class BroadcastHandshakeTest extends AbstractBroadcastTest {
  @Test
  public void testHandshake() throws Exception {
    Injector inj =
        Guice.createInjector(Modules
            .override(new ProtobeeGuiceModule(), new BroadcastGuiceModule()).with(
                new LocalChannelsModule()));

    ProtobeeServantBootstrapper bootstrap = inj.getInstance(ProtobeeServantBootstrapper.class);
    bootstrap.startup();

    LocalNettyTester tester = createLocalNettyTester();
    tester.connect(new LocalAddress(LocalChannelsModule.LOCAL_ADDRESS), new LocalAddress("test"));
    basicHandshake(tester);
    tester.verifyNotClosed();

    bootstrap.shutdown(1000);
  }

  @Test
  public void testSendingHandshakeNoTimed() throws Exception {
    Injector inj =
        Guice.createInjector(Modules
            .override(new ProtobeeGuiceModule(), new BroadcastGuiceModule()).with(
                new LocalChannelsModule()));

    ProtobeeServantBootstrapper bootstrap = inj.getInstance(ProtobeeServantBootstrapper.class);
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
  public void testSendingHandshakeNoTimeModule() throws Exception {
    Injector inj =
        Guice.createInjector(Modules
            .override(new ProtobeeGuiceModule(), new BroadcastGuiceModule()).with(
                new LocalChannelsModule(), new AbstractModule() {

                  @Override
                  protected void configure() {}

                  @SuppressWarnings("unused")
                  @Provides
                  @Broadcast
                  @SessionScope
                  public Set<ProtocolModule> getModules(
                      Provider<BroadcastMessageModule> messageModule) {
                    Set<ProtocolModule> modules = Sets.newHashSet();
                    modules.add(messageModule.get());
                    return modules;
                  }

                  @SuppressWarnings("unused")
                  @Provides
                  @Broadcast
                  @Singleton
                  public Set<Class<? extends ProtocolModule>> getModuleClasses() {
                    Set<Class<? extends ProtocolModule>> modules = Sets.newHashSet();
                    modules.add(BroadcastMessageModule.class);
                    return modules;
                  }
                }));

    ProtobeeServantBootstrapper bootstrap = inj.getInstance(ProtobeeServantBootstrapper.class);
    bootstrap.startup();


    LocalNettyTester tester = createLocalNettyTester();
    tester.bind(new LocalAddress("test"));

    ProtocolConfig broadcastConfig = getConfig(inj, Broadcast.class);
    ProtocolModel model = fromConfig(inj, broadcastConfig);

    ConnectionCreator creator = inj.getInstance(ConnectionCreator.class);
    creator.connect(model, new LocalAddress("test"), HttpMethod.valueOf("SAY"), "/");

    tester.verifyReceived(ChannelBuffers.wrappedBuffer(handshake0.getBytes()));
    tester.writeAndWait(ChannelBuffers.wrappedBuffer(handshake1.getBytes()), 1000);
    tester.verifyReceived(ChannelBuffers.wrappedBuffer(handshake2.getBytes()));

    tester.verifyNotClosed();

    bootstrap.shutdown(1000);
  }

  @Test
  public void testSendingHandshakeTimed() throws Exception {
    Injector inj =
        Guice.createInjector(Modules
            .override(new ProtobeeGuiceModule(), new BroadcastGuiceModule()).with(
                new LocalChannelsModule()));

    ProtobeeServantBootstrapper bootstrap = inj.getInstance(ProtobeeServantBootstrapper.class);
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
    Injector inj =
        Guice.createInjector(Modules
            .override(new ProtobeeGuiceModule(), new BroadcastGuiceModule()).with(
                new LocalChannelsModule()));

    ProtobeeServantBootstrapper bootstrap = inj.getInstance(ProtobeeServantBootstrapper.class);
    bootstrap.startup();

    LocalNettyTester tester = createLocalNettyTester();
    tester.connect(new LocalAddress(LocalChannelsModule.LOCAL_ADDRESS), new LocalAddress("test"));
    timedHandshake(tester);
    tester.verifyNotClosed();

    bootstrap.shutdown(1000);
  }
}

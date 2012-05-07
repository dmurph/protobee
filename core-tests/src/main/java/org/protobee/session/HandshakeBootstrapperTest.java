package org.protobee.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.net.SocketAddress;
import java.util.Set;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.junit.Test;
import org.protobee.AbstractTest;
import org.protobee.guice.scopes.SessionScope;
import org.protobee.identity.NetworkIdentity;
import org.protobee.identity.NetworkIdentityManager;
import org.protobee.protocol.ProtocolConfig;
import org.protobee.session.handshake.HandshakeHandlers;
import org.protobee.session.handshake.HandshakeStateBootstrapper;

import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.util.Modules;

public class HandshakeBootstrapperTest extends AbstractTest {

  @Test
  public void testNewSession() {
    ProtocolConfig config = mockDefaultProtocolConfig();

    Injector inj = getInjectorWithProtocolConfig(config);

    NetworkIdentityManager manager = inj.getInstance(NetworkIdentityManager.class);
    NetworkIdentity identity = manager.createNetworkIdentity();

    SocketAddress listenAddress = createAddress("2.1.3.4", 23);
    SocketAddress sendingAddress = createAddress("2.1.3.4", 25);
    manager.setListeningAddress(identity, config.get(), listenAddress);
    manager.setSendingAddress(identity, config.get(), sendingAddress);

    Channel channel = mock(Channel.class);
    ChannelPipeline pipeline = mock(ChannelPipeline.class);

    HandshakeStateBootstrapper handshake = inj.getInstance(HandshakeStateBootstrapper.class);

    handshake.bootstrapSession(fromConfig(inj, config), identity, channel, pipeline);

    SessionModel session = identity.getCurrentSession(config.get());
    assertNotNull(session);
  }

  @Test
  public void testStateInitialized() {
    ProtocolConfig config = mockDefaultProtocolConfig();

    Injector inj = getInjectorWithProtocolConfig(config);

    NetworkIdentityManager manager = inj.getInstance(NetworkIdentityManager.class);
    NetworkIdentity identity = manager.createNetworkIdentity();

    SocketAddress listenAddress = createAddress("2.1.3.4", 23);
    SocketAddress sendingAddress = createAddress("2.1.3.4", 25);
    manager.setListeningAddress(identity, config.get(), listenAddress);
    manager.setSendingAddress(identity, config.get(), sendingAddress);

    Channel channel = mock(Channel.class);
    ChannelPipeline pipeline = mock(ChannelPipeline.class);

    HandshakeStateBootstrapper handshake = inj.getInstance(HandshakeStateBootstrapper.class);

    handshake.bootstrapSession(fromConfig(inj, config), identity, channel, pipeline);

    SessionModel session = identity.getCurrentSession(config.get());
    assertEquals(SessionState.HANDSHAKE_0, session.getSessionState());
  }

  @Test
  public void testHandlersAdded() {
    ProtocolConfig config = mockDefaultProtocolConfig();

    ChannelHandler handler = mock(ChannelHandler.class);
    final Set<ChannelHandler> handlers = Sets.newHashSet(handler); 

    Injector inj =
        getInjector(Modules.combine(getModuleWithProtocolConfig(config), new AbstractModule() {
          @Override
          protected void configure() {}
          @SuppressWarnings("unused")
          @Provides
          @HandshakeHandlers
          @SessionScope
          public Set<ChannelHandler> handlers() {
            return handlers;
          }
        }));

    NetworkIdentityManager manager = inj.getInstance(NetworkIdentityManager.class);
    NetworkIdentity identity = manager.createNetworkIdentity();

    SocketAddress listenAddress = createAddress("2.1.3.4", 23);
    SocketAddress sendingAddress = createAddress("2.1.3.4", 25);
    manager.setListeningAddress(identity, config.get(), listenAddress);
    manager.setSendingAddress(identity, config.get(), sendingAddress);

    Channel channel = mock(Channel.class);
    ChannelPipeline pipeline = mock(ChannelPipeline.class);

    HandshakeStateBootstrapper handshake = inj.getInstance(HandshakeStateBootstrapper.class);

    handshake.bootstrapSession(fromConfig(inj, config), identity, channel, pipeline);

    verify(pipeline).addLast(anyString(), eq(handler));
  }
}

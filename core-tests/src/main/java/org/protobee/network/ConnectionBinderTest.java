package org.protobee.network;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Set;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.junit.Test;
import org.protobee.AbstractTest;
import org.protobee.identity.NetworkIdentityManager;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolConfig;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

public class ConnectionBinderTest extends AbstractTest {

  @Test
  public void testSingleConfigBound() {
    int port = 10;
    SocketAddress address = new InetSocketAddress(port);
    ProtocolConfig config = mockDefaultProtocolConfig();
    when(config.getListeningAddress()).thenReturn(address);

    final ProtobeeChannels channels = mock(ProtobeeChannels.class);
    final ServerBootstrap bootstrap = mock(ServerBootstrap.class);
    Channel channel = mock(Channel.class);
    when(bootstrap.bind(any(SocketAddress.class))).thenReturn(channel);

    Injector inj =
        getInjector(Modules.combine(getModuleWithProtocolConfig(config), new AbstractModule() {
          @Override
          protected void configure() {
            bind(ServerBootstrap.class).toInstance(bootstrap);
            bind(ProtobeeChannels.class).toInstance(channels);
          }
        }));

    ConnectionBinder binder = inj.getInstance(ConnectionBinder.class);

    Channel bindedChannel = binder.bind(getModelFor(inj, config.get()));

    InetSocketAddress localAddress = new InetSocketAddress(port);

    assertEquals(channel, bindedChannel);
    verify(bootstrap).setOptions(eq(config.getServerOptions()));
    verify(bootstrap).bind(eq(localAddress));
    Protocol protocol = config.get();
    verify(channels).addChannel(eq(channel), eq(protocol));

    NetworkIdentityManager identities = inj.getInstance(NetworkIdentityManager.class);

    assertEquals(address, identities.getMe().getListeningAddress(protocol));

  }

  @Test
  public void testMultipleBound() {
    ChannelFactory serverFactory = mock(ChannelFactory.class);
    int port = 10;
    SocketAddress address = new InetSocketAddress(port);
    ProtocolConfig config = mockDefaultProtocolConfig();
    ProtocolConfig config2 = mockDefaultProtocolConfig();
    when(config.getListeningAddress()).thenReturn(address);
    when(config.getServerChannelFactory()).thenReturn(serverFactory);
    when(config2.getListeningAddress()).thenReturn(address);
    when(config2.getServerChannelFactory()).thenReturn(serverFactory);
    Set<ProtocolConfig> configs = ImmutableSet.of(config, config2);

    final ProtobeeChannels channels = mock(ProtobeeChannels.class);
    final ServerBootstrap bootstrap = mock(ServerBootstrap.class);
    Channel channel = mock(Channel.class);
    when(bootstrap.bind(any(SocketAddress.class))).thenReturn(channel);

    Injector inj =
        getInjector(Modules.combine(getModuleWithProtocolConfig(config, config2),
            new AbstractModule() {
              @Override
              protected void configure() {
                bind(ServerBootstrap.class).toInstance(bootstrap);
                bind(ProtobeeChannels.class).toInstance(channels);
              }
            }));

    ConnectionBinder binder = inj.getInstance(ConnectionBinder.class);

    Channel bindedChannel = binder.bind(fromConfigs(inj, configs), address);

    assertEquals(channel, bindedChannel);
    verify(bootstrap).bind(eq(address));

    Protocol protocol1 = config.get();
    Protocol protocol2 = config2.get();
    verify(channels).addChannel(eq(channel), eq(ImmutableSet.<Protocol>of(protocol1, protocol2)));

    NetworkIdentityManager identities = inj.getInstance(NetworkIdentityManager.class);

    assertEquals(address, identities.getMe().getListeningAddress(protocol1));
    assertEquals(address, identities.getMe().getListeningAddress(protocol2));
  }

}

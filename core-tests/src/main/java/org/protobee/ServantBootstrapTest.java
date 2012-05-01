package org.protobee;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.util.Set;

import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.junit.Test;
import org.protobee.network.ConnectionBinder;
import org.protobee.network.ProtobeeChannels;
import org.protobee.protocol.ProtocolConfig;

import com.google.common.collect.ImmutableSet;


public class ServantBootstrapTest {


  @Test
  public void testSingleBind() {
    ProtocolConfig config = mock(ProtocolConfig.class);
    when(config.getListeningAddress()).thenReturn(new InetSocketAddress(10));

    ConnectionBinder binder = mock(ConnectionBinder.class);
    ChannelFactory channelFactory = mock(ChannelFactory.class);
    ProtobeeChannels channels = mock(ProtobeeChannels.class);

    JnutellaServantBootstrapper bootstrapper =
        new JnutellaServantBootstrapper(ImmutableSet.of(config), binder, channelFactory, channels);

    bootstrapper.startup();

    verify(binder).bind(config);
  }

  @Test
  public void testSamePort() {
    ProtocolConfig config = mock(ProtocolConfig.class);
    ProtocolConfig config2 = mock(ProtocolConfig.class);
    when(config.getListeningAddress()).thenReturn(new InetSocketAddress(10));
    when(config2.getListeningAddress()).thenReturn(new InetSocketAddress(10));

    Set<ProtocolConfig> configs = ImmutableSet.of(config, config2);

    ConnectionBinder binder = mock(ConnectionBinder.class);
    ChannelFactory channelFactory = mock(ChannelFactory.class);
    ProtobeeChannels channels = mock(ProtobeeChannels.class);

    JnutellaServantBootstrapper bootstrapper =
        new JnutellaServantBootstrapper(configs, binder, channelFactory, channels);

    bootstrapper.startup();

    verify(binder).bind(eq(configs), eq(new InetSocketAddress(10)));
  }

  @Test
  public void testShutdown() {
    ProtocolConfig config = mock(ProtocolConfig.class);
    when(config.getListeningAddress()).thenReturn(new InetSocketAddress(10));

    ConnectionBinder binder = mock(ConnectionBinder.class);
    ChannelFactory channelFactory = mock(ChannelFactory.class);
    ProtobeeChannels channels = mock(ProtobeeChannels.class);
    ChannelGroup channelGroup = mock(ChannelGroup.class);
    ChannelGroupFuture closeFuture = mock(ChannelGroupFuture.class);
    when(channels.getChannels()).thenReturn(channelGroup);
    when(channelGroup.close()).thenReturn(closeFuture);

    JnutellaServantBootstrapper bootstrapper =
        new JnutellaServantBootstrapper(ImmutableSet.of(config), binder, channelFactory, channels);

    bootstrapper.startup();

    long waitTime = 1000;
    bootstrapper.shutdown(waitTime);

    verify(channels).getChannels();
    verify(channelGroup).close();

    verify(closeFuture).awaitUninterruptibly(eq(waitTime));

    verify(channelFactory).releaseExternalResources();
    verify(channels).clear();
  }
}

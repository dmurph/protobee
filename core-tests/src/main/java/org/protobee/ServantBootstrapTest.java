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
import org.protobee.protocol.ProtocolModel;

import com.google.common.collect.ImmutableSet;


public class ServantBootstrapTest {


  @Test
  public void testSingleBind() {
    ProtocolModel model = mock(ProtocolModel.class);
    when(model.getLocalListeningAddress()).thenReturn(new InetSocketAddress(10));

    ConnectionBinder binder = mock(ConnectionBinder.class);
    ChannelFactory channelFactory = mock(ChannelFactory.class);
    ProtobeeChannels channels = mock(ProtobeeChannels.class);

    JnutellaServantBootstrapper bootstrapper =
        new JnutellaServantBootstrapper(ImmutableSet.of(model), binder, channelFactory, channels);

    bootstrapper.startup();

    verify(binder).bind(model);
  }

  @Test
  public void testSamePort() {
    ProtocolModel model = mock(ProtocolModel.class);
    when(model.getLocalListeningAddress()).thenReturn(new InetSocketAddress(10));
    ProtocolModel model2 = mock(ProtocolModel.class);
    when(model2.getLocalListeningAddress()).thenReturn(new InetSocketAddress(10));

    Set<ProtocolModel> models = ImmutableSet.of(model, model2);

    ConnectionBinder binder = mock(ConnectionBinder.class);
    ChannelFactory channelFactory = mock(ChannelFactory.class);
    ProtobeeChannels channels = mock(ProtobeeChannels.class);

    JnutellaServantBootstrapper bootstrapper =
        new JnutellaServantBootstrapper(models, binder, channelFactory, channels);

    bootstrapper.startup();

    verify(binder).bind(eq(models), eq(new InetSocketAddress(10)));
  }

  @Test
  public void testShutdown() {
    ProtocolModel model = mock(ProtocolModel.class);
    when(model.getLocalListeningAddress()).thenReturn(new InetSocketAddress(10));

    ConnectionBinder binder = mock(ConnectionBinder.class);
    ChannelFactory channelFactory = mock(ChannelFactory.class);
    ProtobeeChannels channels = mock(ProtobeeChannels.class);
    ChannelGroup channelGroup = mock(ChannelGroup.class);
    ChannelGroupFuture closeFuture = mock(ChannelGroupFuture.class);
    when(channels.getChannels()).thenReturn(channelGroup);
    when(channelGroup.close()).thenReturn(closeFuture);

    JnutellaServantBootstrapper bootstrapper =
        new JnutellaServantBootstrapper(ImmutableSet.of(model), binder, channelFactory, channels);

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

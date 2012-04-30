package org.protobee.network;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.UpstreamMessageEvent;
import org.junit.Test;
import org.protobee.AbstractTest;
import org.protobee.identity.NetworkIdentity;
import org.protobee.identity.NetworkIdentityManager;
import org.protobee.network.handlers.MultipleRequestReceiver;
import org.protobee.network.handlers.SingleRequestReceiver;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolConfig;
import org.protobee.session.handshake.HandshakeStateBootstrapper;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

public class RequestReceiverTest extends AbstractTest {

  @Test
  public void testSingleRequestReceiverMatch() {
    ProtocolConfig config = mockDefaultProtocolConfig();
    Protocol protocol = mock(Protocol.class);
    when(protocol.headerRegex()).thenReturn("^TEST CONNECT/0\\.6$");
    when(config.get()).thenReturn(protocol);

    Channel channel = mock(Channel.class);
    InetSocketAddress remoteAddress = createAddress("6.5.2.4", 104);
    when(channel.getRemoteAddress()).thenReturn(remoteAddress);

    ChannelPipeline pipeline = mock(ChannelPipeline.class);

    final ChannelHandlerContext context = mock(ChannelHandlerContext.class);
    when(context.getPipeline()).thenReturn(pipeline);
    when(context.getChannel()).thenReturn(channel);

    final HandshakeStateBootstrapper handshakeBootstrapper = mock(HandshakeStateBootstrapper.class);
    final ProtobeeChannels channels = mock(ProtobeeChannels.class);

    Injector inj =
        getInjector(Modules.combine(getModuleWithProtocolConfig(config), new AbstractModule() {
          @Override
          protected void configure() {
            bind(HandshakeStateBootstrapper.class).toInstance(handshakeBootstrapper);
            bind(ProtobeeChannels.class).toInstance(channels);
          }
        }));

    SingleRequestReceiver.Factory factory = inj.getInstance(SingleRequestReceiver.Factory.class);

    SingleRequestReceiver receiver = spy(factory.create(config));
    doReturn(ChannelBuffers.dynamicBuffer()).when(receiver).newCumulationBuffer(
        any(ChannelHandlerContext.class), anyInt());

    String headerString = "TEST CONNECT/0.6\r\njaslk";
    ChannelBuffer header = ChannelBuffers.dynamicBuffer();
    header.writeBytes(headerString.getBytes());

    try {
      receiver.handleUpstream(context, new UpstreamMessageEvent(channel, header, remoteAddress));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    NetworkIdentityManager manager = inj.getInstance(NetworkIdentityManager.class);
    NetworkIdentity identity = manager.getNewtorkIdentity(remoteAddress);

    assertNotNull(identity);

    verify(channels).addChannel(eq(channel), eq(protocol));

    verify(pipeline).remove(receiver);

    verify(handshakeBootstrapper).bootstrapSession(eq(config), eq(identity), eq(channel),
        any(ChannelPipeline.class));
  }

  @Test
  public void testCloseOnNoMatch() {
    ProtocolConfig config = mockDefaultProtocolConfig();
    Protocol protocol = mock(Protocol.class);
    when(protocol.headerRegex()).thenReturn("^TEST CONNECT/0\\.6$");
    when(config.get()).thenReturn(protocol);

    Channel channel = mock(Channel.class);
    InetSocketAddress remoteAddress = createAddress("6.5.2.4", 104);
    when(channel.getRemoteAddress()).thenReturn(remoteAddress);

    ChannelPipeline pipeline = mock(ChannelPipeline.class);

    final ChannelHandlerContext context = mock(ChannelHandlerContext.class);
    when(context.getPipeline()).thenReturn(pipeline);
    when(context.getChannel()).thenReturn(channel);

    final HandshakeStateBootstrapper handshakeBootstrapper = mock(HandshakeStateBootstrapper.class);
    final ProtobeeChannels channels = mock(ProtobeeChannels.class);

    Injector inj =
        getInjector(Modules.combine(getModuleWithProtocolConfig(config), new AbstractModule() {
          @Override
          protected void configure() {
            bind(HandshakeStateBootstrapper.class).toInstance(handshakeBootstrapper);
            bind(ProtobeeChannels.class).toInstance(channels);
          }
        }));

    SingleRequestReceiver.Factory factory = inj.getInstance(SingleRequestReceiver.Factory.class);

    SingleRequestReceiver receiver = spy(factory.create(config));
    doReturn(ChannelBuffers.dynamicBuffer()).when(receiver).newCumulationBuffer(
        any(ChannelHandlerContext.class), anyInt());

    String headerString = "NOPE CONNECT/0.6\r\njaslk";
    ChannelBuffer header = ChannelBuffers.dynamicBuffer();
    header.writeBytes(headerString.getBytes());

    try {
      receiver.handleUpstream(context, new UpstreamMessageEvent(channel, header, remoteAddress));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    verify(channel).close();
  }

  @Test
  public void testMatchOnMultiple() {
    ProtocolConfig config = mockDefaultProtocolConfig();
    Protocol protocol = mock(Protocol.class);
    when(protocol.headerRegex()).thenReturn("^TEST CONNECT/0\\.6$");
    when(config.get()).thenReturn(protocol);


    ProtocolConfig config2 = mockDefaultProtocolConfig();
    Protocol protocol2 = mock(Protocol.class);
    when(protocol2.headerRegex()).thenReturn("^TEST2 CONNECT/0\\.6$");
    when(config2.get()).thenReturn(protocol2);

    Set<ProtocolConfig> configs = ImmutableSet.of(config, config2);

    Channel channel = mock(Channel.class);
    InetSocketAddress remoteAddress = createAddress("6.5.2.4", 104);
    when(channel.getRemoteAddress()).thenReturn(remoteAddress);

    ChannelPipeline pipeline = mock(ChannelPipeline.class);

    final ChannelHandlerContext context = mock(ChannelHandlerContext.class);
    when(context.getPipeline()).thenReturn(pipeline);
    when(context.getChannel()).thenReturn(channel);

    final HandshakeStateBootstrapper handshakeBootstrapper = mock(HandshakeStateBootstrapper.class);
    final ProtobeeChannels channels = mock(ProtobeeChannels.class);

    Injector inj =
        getInjector(Modules.combine(getModuleWithProtocolConfig(config, config2), new AbstractModule() {
          @Override
          protected void configure() {
            bind(HandshakeStateBootstrapper.class).toInstance(handshakeBootstrapper);
            bind(ProtobeeChannels.class).toInstance(channels);
          }
        }));

    MultipleRequestReceiver.Factory factory =
        inj.getInstance(MultipleRequestReceiver.Factory.class);

    MultipleRequestReceiver receiver = spy(factory.create(configs));
    doReturn(ChannelBuffers.dynamicBuffer()).when(receiver).newCumulationBuffer(
        any(ChannelHandlerContext.class), anyInt());

    String headerString = "TEST2 CONNECT/0.6\r\njaslk";
    ChannelBuffer header = ChannelBuffers.dynamicBuffer();
    header.writeBytes(headerString.getBytes());

    try {
      receiver.handleUpstream(context, new UpstreamMessageEvent(channel, header, remoteAddress));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    verify(handshakeBootstrapper).bootstrapSession(eq(config2), any(NetworkIdentity.class),
        eq(channel), any(ChannelPipeline.class));
    verify(handshakeBootstrapper, never()).bootstrapSession(eq(config), any(NetworkIdentity.class),
      eq(channel), any(ChannelPipeline.class));
  }
}

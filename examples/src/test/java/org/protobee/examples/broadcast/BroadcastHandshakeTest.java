package org.protobee.examples.broadcast;

import static org.mockito.Mockito.*;

import java.net.SocketAddress;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelDownstreamHandler;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.local.DefaultLocalClientChannelFactory;
import org.jboss.netty.channel.local.LocalAddress;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.protobee.AbstractTest;
import org.protobee.JnutellaServantBootstrapper;
import org.protobee.ProtobeeGuiceModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class BroadcastHandshakeTest extends AbstractTest {

  private static final Logger log = LoggerFactory.getLogger(BroadcastHandshakeTest.class);

  private String handshake = "SAY / BROADCAST/0.1\r\n";

  @Test
  public void testNoExceptions() throws Exception {
    Injector inj = Guice.createInjector(new ProtobeeGuiceModule(), new BroadcastGuiceModule());

    JnutellaServantBootstrapper bootstrap = inj.getInstance(JnutellaServantBootstrapper.class);
    bootstrap.startup();
    

    final SocketAddress remoteAddress = new LocalAddress("broadcast-example");
    final SimpleChannelUpstreamHandler reader = spy(new SimpleChannelUpstreamHandler());

    ClientBootstrap testBootstrap = new ClientBootstrap(new DefaultLocalClientChannelFactory());

    testBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
      @Override
      public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("reader", reader);
        return pipeline;
      }
    });

    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        log.info("got " + invocation.getArguments()[1]);
        return null;
      }
    }).when(reader).messageReceived(any(ChannelHandlerContext.class), any(MessageEvent.class));

    ChannelFuture connect =
        testBootstrap.connect(remoteAddress, new LocalAddress("test"));
    
    Channel channel = connect.getChannel();
    Channels.write(channel, handshake, remoteAddress);
    
    bootstrap.shutdown(10000);
  }
}

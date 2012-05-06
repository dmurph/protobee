package org.protobee.netty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.local.DefaultLocalClientChannelFactory;
import org.jboss.netty.channel.local.LocalAddress;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.logging.InternalLogLevel;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class LocalNettyTester {
  private static final Logger log = LoggerFactory.getLogger(LocalNettyTester.class);

  private final AtomicBoolean started = new AtomicBoolean(false);
  private final AtomicReference<ChannelBuffer> received = new AtomicReference<ChannelBuffer>(null);
  private final ChannelUpstreamHandler receiver;
  private Channel channel;

  public LocalNettyTester() {
    receiver = mock(ChannelUpstreamHandler.class);
    try {
      doAnswer(new Answer<Void>() {
        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
          MessageEvent event = (MessageEvent) invocation.getArguments()[1];
          if (!(event.getMessage() instanceof ChannelBuffer)) {
            log.error("Didn't get a channel buffer from message event " + event);
          } else if (!received.compareAndSet(null, (ChannelBuffer) event.getMessage())) {
            log.error("Received buffer wasn't null, setting anyways");
            received.set((ChannelBuffer) event.getMessage());
          }
          return null;
        }
      }).when(receiver).handleUpstream(any(ChannelHandlerContext.class), isA(MessageEvent.class));
    } catch (Exception e) {
      // should never get here
      log.error("Unexpected exception", e);
    }
  }

  public void connect(LocalAddress remote, LocalAddress local) {
    Preconditions.checkState(started.compareAndSet(false, true), "Already started");

    ClientBootstrap testBootstrap = new ClientBootstrap(new DefaultLocalClientChannelFactory());

    testBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
      @Override
      public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("logger", new LoggingHandler("org.protobee.netty.TestLogger",
            InternalLogLevel.DEBUG, false));
        pipeline.addLast("reader", receiver);
        return pipeline;
      }
    });

    ChannelFuture connect = testBootstrap.connect(remote, local);
    connect.awaitUninterruptibly(1000);
    channel = connect.getChannel();
  }

  public ChannelUpstreamHandler getMockedReceiver() {
    return receiver;
  }

  public void writeAndWait(ChannelBuffer data, long maxWait) {
    clearReceived();
    Channels.write(channel, data).awaitUninterruptibly(maxWait);
  }
  
  public void clearReceived() {
    received.set(null);
  }

  public void verifyReceived(ChannelBuffer expected) {
    assertNotNull("No data received", received.get());
    ChannelBuffer buffer = received.get();
    assertEquals(expected, buffer);
  }
  
  public void verifyNothingReceived() {
    assertNull(received.get());
  }

  public void verifyNotClosed() {
    assertTrue(channel.isConnected());
  }
}

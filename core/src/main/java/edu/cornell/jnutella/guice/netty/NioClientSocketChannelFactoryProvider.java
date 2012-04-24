package edu.cornell.jnutella.guice.netty;

import java.util.concurrent.Executor;

import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.google.inject.Inject;

public class NioClientSocketChannelFactoryProvider
    extends AbstractChannelFactoryProvider<NioClientSocketChannelFactory> {

  @Inject
  public NioClientSocketChannelFactoryProvider(@ChannelFactoryWorker Executor workerExecutor,
      @ChannelFactoryBoss Executor bossExecutor) {
    super(workerExecutor, bossExecutor);
  }

  public NioClientSocketChannelFactory get() {
    return new NioClientSocketChannelFactory(workerExecutor, bossExecutor);
  }
}

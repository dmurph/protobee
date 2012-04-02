package edu.cornell.jnutella.guice.netty;

import java.util.concurrent.Executor;

import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.google.inject.Inject;

public class NioServerSocketChannelFactoryProvider
    extends AbstractChannelFactoryProvider<NioServerSocketChannelFactory> {

  @Inject
  public NioServerSocketChannelFactoryProvider(@ChannelFactoryWorker Executor workerExecutor,
      @ChannelFactoryBoss Executor bossExecutor) {
    super(workerExecutor, bossExecutor);
  }

  public NioServerSocketChannelFactory get() {
    return new NioServerSocketChannelFactory(workerExecutor, bossExecutor);
  }
}

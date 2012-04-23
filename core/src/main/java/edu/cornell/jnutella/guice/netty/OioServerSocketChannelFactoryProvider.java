package edu.cornell.jnutella.guice.netty;

import java.util.concurrent.Executor;

import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;

import com.google.inject.Inject;

public class OioServerSocketChannelFactoryProvider
    extends AbstractChannelFactoryProvider<OioServerSocketChannelFactory> {

  @Inject
  public OioServerSocketChannelFactoryProvider(@ChannelFactoryWorker Executor workerExecutor,
      @ChannelFactoryBoss Executor bossExecutor) {
    super(workerExecutor, bossExecutor);
  }

  public OioServerSocketChannelFactory get() {
    return new OioServerSocketChannelFactory(workerExecutor, bossExecutor);
  }
}

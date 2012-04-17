package edu.cornell.jnutella.guice.netty;

import java.util.concurrent.Executor;

import org.jboss.netty.channel.ChannelFactory;

import com.google.inject.Provider;

public abstract class AbstractChannelFactoryProvider<T extends ChannelFactory>
    implements
      Provider<T> {

  protected final Executor workerExecutor;
  protected final Executor bossExecutor;

  protected AbstractChannelFactoryProvider(Executor workerExecutor, Executor bossExecutor) {
    if (workerExecutor == null || bossExecutor == null) {
      throw new NullPointerException("executor");
    }
    this.workerExecutor = workerExecutor;
    this.bossExecutor = bossExecutor;
  }
}

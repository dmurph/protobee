package org.protobee.guice.netty;

import java.util.concurrent.Executor;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Singleton;


public class ExecutorModule extends AbstractModule {

  private final Provider<Executor> bossExecutor;
  private final Provider<Executor> workerExecutor;

  public ExecutorModule(Provider<Executor> bossExecutor, Provider<Executor> workerExecutor) {
    this.bossExecutor = bossExecutor;
    this.workerExecutor = workerExecutor;
  }

  @Override
  protected void configure() {
    bind(Executor.class).annotatedWith(ChannelFactoryBoss.class).toProvider(bossExecutor)
        .in(Singleton.class);
    bind(Executor.class).annotatedWith(ChannelFactoryWorker.class).toProvider(workerExecutor)
        .in(Singleton.class);
  }
}

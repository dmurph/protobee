package edu.cornell.jnutella.guice.netty;

import java.util.concurrent.Executor;

import com.google.inject.AbstractModule;


public class ExecutorModule extends AbstractModule {

  private final Executor bossExecutor;
  private final Executor workerExecutor;
  
  public ExecutorModule(Executor bossExecutor, Executor workerExecutor) {
    this.bossExecutor = bossExecutor;
    this.workerExecutor = workerExecutor;
  }

  @Override
  protected void configure() {
    bind(Executor.class).annotatedWith(ChannelFactoryBoss.class).toInstance(bossExecutor);
    bind(Executor.class).annotatedWith(ChannelFactoryWorker.class).toInstance(workerExecutor);
  }
}

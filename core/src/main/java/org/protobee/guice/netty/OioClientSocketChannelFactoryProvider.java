package org.protobee.guice.netty;

import java.util.concurrent.Executor;

import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;

import com.google.inject.Inject;

public class OioClientSocketChannelFactoryProvider
    extends AbstractChannelFactoryProvider<OioClientSocketChannelFactory> {

  @Inject
  public OioClientSocketChannelFactoryProvider(@ChannelFactoryWorker Executor workerExecutor) {
    super(workerExecutor, workerExecutor);
  }

  public OioClientSocketChannelFactory get() {
    return new OioClientSocketChannelFactory(workerExecutor);
  }
}

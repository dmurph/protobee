package edu.cornell.jnutella;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.group.ChannelGroupFuture;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import edu.cornell.jnutella.network.ConnectionBinder;
import edu.cornell.jnutella.network.JnutellaChannels;
import edu.cornell.jnutella.protocol.ProtocolConfig;

/**
 * bootstraps the jnutella servant, binding all protocols
 * 
 * @author Daniel
 */
@Singleton
public class JnutellaServantBootstrapper {

  private final Set<ProtocolConfig> protocols;
  private final ConnectionBinder connectionBinder;
  private final ChannelFactory channelFactory;
  private final JnutellaChannels channels;
  private final AtomicBoolean started = new AtomicBoolean(false);
  private final AtomicBoolean shuttingDown = new AtomicBoolean(false);
  private final Object lock = new Object();

  @Inject
  public JnutellaServantBootstrapper(Set<ProtocolConfig> protocols,
      ConnectionBinder connectionBinder, ChannelFactory factory, JnutellaChannels channels) {
    this.protocols = protocols;
    this.connectionBinder = connectionBinder;
    this.channelFactory = factory;
    this.channels = channels;
  }

  public boolean isStarted() {
    return started.get();
  }

  public void startup() {
    Preconditions.checkState(started.compareAndSet(false, true),
        "Jnutella servant was already started");
    synchronized (lock) {
      if (shuttingDown.get()) {
        started.set(false);
        throw new IllegalStateException("We're currently shutting down the servant");
      }
      HashMultimap<Integer, ProtocolConfig> portToProtocols = HashMultimap.create();

      for (ProtocolConfig config : protocols) {
        portToProtocols.put(config.getPort(), config);
      }

      for (Integer port : portToProtocols.keySet()) {
        Set<ProtocolConfig> configs = portToProtocols.get(port);

        if (configs.size() == 1) {
          connectionBinder.bind(Iterables.getOnlyElement(configs));
        } else {
          connectionBinder.bind(configs, port);
        }
      }
    }

  }

  public void shutdown(int maxWaitMillis) {
    Preconditions.checkState(shuttingDown.compareAndSet(false, true),
        "Jnutella servant was already started");
    synchronized (lock) {
      Preconditions
          .checkState(started.compareAndSet(true, false), "We haven't started the servant");

      ChannelGroupFuture future = channels.getChannels().close();
      future.awaitUninterruptibly(maxWaitMillis);
      channelFactory.releaseExternalResources();
      channels.clear();
      shuttingDown.set(false);
    }
  }
}

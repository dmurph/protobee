package org.protobee;

import java.net.SocketAddress;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.protobee.guice.scopes.ProtobeeScopes;
import org.protobee.network.ConnectionBinder;
import org.protobee.network.ProtobeeChannels;
import org.protobee.protocol.LocalListeningAddress;
import org.protobee.protocol.ProtocolModel;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;


/**
 * bootstraps the jnutella servant, binding all protocols
 * 
 * @author Daniel
 */
@Singleton
public class JnutellaServantBootstrapper {

  private final Set<ProtocolModel> protocols;
  private final Provider<SocketAddress> localAddressProvider;

  private final ConnectionBinder connectionBinder;
  private final ChannelFactory channelFactory;
  private final ProtobeeChannels channels;
  private final AtomicBoolean started = new AtomicBoolean(false);
  private final AtomicBoolean shuttingDown = new AtomicBoolean(false);
  private final Object lock = new Object();

  @Inject
  public JnutellaServantBootstrapper(Set<ProtocolModel> protocols,
      ConnectionBinder connectionBinder, ChannelFactory factory, ProtobeeChannels channels,
      @LocalListeningAddress Provider<SocketAddress> localAddressProvider) {
    this.protocols = protocols;
    this.connectionBinder = connectionBinder;
    this.channelFactory = factory;
    this.channels = channels;
    this.localAddressProvider = localAddressProvider;
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
      HashMultimap<SocketAddress, ProtocolModel> portToProtocols = HashMultimap.create();

      try {
        for (ProtocolModel model : protocols) {
          model.enterScope();
          portToProtocols.put(localAddressProvider.get(), model);
          model.exitScope();
        }

        for (SocketAddress address : portToProtocols.keySet()) {
          Set<ProtocolModel> models = portToProtocols.get(address);

          if (models.size() == 1) {
            connectionBinder.bind(Iterables.getOnlyElement(models));
          } else {
            connectionBinder.bind(models, address);
          }
        }
      } finally {
        ProtobeeScopes.PROTOCOL.exitScope();
      }
    }
  }

  public void shutdown(long maxWaitMillis) {
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

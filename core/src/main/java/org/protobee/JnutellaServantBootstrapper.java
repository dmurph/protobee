package org.protobee;

import java.net.SocketAddress;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.protobee.guice.scopes.ProtobeeScopes;
import org.protobee.network.ConnectionBinder;
import org.protobee.network.ProtobeeChannels;
import org.protobee.protocol.ProtocolModel;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;


/**
 * bootstraps the jnutella servant, binding all protocols
 * 
 * @author Daniel
 */
@Singleton
public class JnutellaServantBootstrapper {

  private final Set<ProtocolModel> protocols;

  private final ConnectionBinder connectionBinder;
  private final Set<ChannelFactory> serverChannelFactories = Sets.newHashSet();
  private final ProtobeeChannels channels;
  private final AtomicBoolean started = new AtomicBoolean(false);
  private final AtomicBoolean shuttingDown = new AtomicBoolean(false);
  private final Object lock = new Object();

  @Inject
  public JnutellaServantBootstrapper(Set<ProtocolModel> protocols,
      ConnectionBinder connectionBinder, ProtobeeChannels channels) {
    this.protocols = protocols;
    this.connectionBinder = connectionBinder;
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
      HashMultimap<SocketAddress, ProtocolModel> portToProtocols = HashMultimap.create();

      serverChannelFactories.clear();
      try {
        for (ProtocolModel model : protocols) {
          portToProtocols.put(model.getLocalListeningAddress(), model);
        }

        for (SocketAddress address : portToProtocols.keySet()) {
          Set<ProtocolModel> models = portToProtocols.get(address);

          if (models.size() == 1) {
            ProtocolModel model = Iterables.getOnlyElement(models);
            serverChannelFactories.add(model.getServerFactory());
            connectionBinder.bind(Iterables.getOnlyElement(models));
          } else {
            ChannelFactory serverFactory = null;
            for (ProtocolModel protocolModel : models) {
              if (serverFactory == null) {
                serverFactory = protocolModel.getServerFactory();
                Preconditions.checkState(serverFactory != null,
                    "Client or server channel factories were null");
                continue;
              }
              Preconditions
                  .checkState(serverFactory == protocolModel.getServerFactory(),
                      "Protocols with the same listening address must have the same server channel factory");
            }
            serverChannelFactories.add(serverFactory);
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
      for (ChannelFactory factory : serverChannelFactories) {
        factory.releaseExternalResources();
      }
      channels.clear();
      shuttingDown.set(false);
    }
  }
}

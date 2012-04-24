package edu.cornell.jnutella.network;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

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
  private final AtomicBoolean started = new AtomicBoolean(false);

  @Inject
  public JnutellaServantBootstrapper(Set<ProtocolConfig> protocols,
      ConnectionBinder connectionBinder) {
    this.protocols = protocols;
    this.connectionBinder = connectionBinder;
  }

  public void startup() {
    Preconditions.checkState(started.compareAndSet(false, true),
        "Jnutella servant was already started");

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

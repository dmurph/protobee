package edu.cornell.jnutella.guice;

import java.util.Map;
import java.util.Set;

import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.ProvisionException;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import edu.cornell.jnutella.gnutella.GnutellaGuiceModule;
import edu.cornell.jnutella.identity.NetworkIdentityManager;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.ProtocolConfig;
import edu.cornell.jnutella.protocol.headers.CompatabilityHeaderMerger;

public class JnutellaMainModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new GnutellaGuiceModule());
    install(new LogModule());
    install(new FactoryModuleBuilder().build(CompatabilityHeaderMerger.Factory.class));

    InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

    bind(NetworkIdentityManager.class).in(Singleton.class);
  }

  @Provides
  @Singleton
  public Map<Protocol, ProtocolConfig> getConfigMap(Set<ProtocolConfig> protocols) {
    ImmutableMap.Builder<Protocol, ProtocolConfig> builder = ImmutableMap.builder();
    for (ProtocolConfig protocolConfig : protocols) {
      Protocol protocol = protocolConfig.getClass().getAnnotation(Protocol.class);
      if (protocol == null) {
        throw new ProvisionException("Protocol config not annotated with protocol: "
            + protocolConfig);
      }
      if (protocolConfig.get() != protocol) {
        throw new ProvisionException(
            "Protocol annotation on config does not match the one provided by get() for config "
                + protocolConfig);
      }
      builder.put(protocol, protocolConfig);
    }
    return builder.build();
  }
}

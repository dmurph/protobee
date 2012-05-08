package org.protobee.protocol;

import java.util.Set;

import org.jboss.netty.channel.ChannelFactory;
import org.protobee.guice.netty.ClientFactory;
import org.protobee.guice.netty.ServerFactory;
import org.protobee.guice.scopes.NewProtocolScopeHolder;
import org.protobee.guice.scopes.ScopeHolder;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;

public class ProtocolModelsProvider implements Provider<Set<ProtocolModel>> {

  private final Set<ProtocolConfig> configs;
  private final Provider<ScopeHolder> protocolScopeProvider;
  private final Provider<ProtocolModel> protocolModelProvider;

  @Inject
  public ProtocolModelsProvider(Set<ProtocolConfig> protocolConfigs,
      @NewProtocolScopeHolder Provider<ScopeHolder> protocolScopeProvider,
      Provider<ProtocolModel> protocolModelProvider) {
    this.configs = protocolConfigs;
    this.protocolScopeProvider = protocolScopeProvider;
    this.protocolModelProvider = protocolModelProvider;
  }

  @Override
  public Set<ProtocolModel> get() {
    ImmutableSet.Builder<ProtocolModel> scopes = ImmutableSet.builder();

    for (ProtocolConfig config : configs) {
      ScopeHolder holder = protocolScopeProvider.get();
      holder.putInScope(Key.get(ProtocolConfig.class), config);
      holder.putInScope(Key.get(ChannelFactory.class, ClientFactory.class),
          config.getClientChannelFactory());
      holder.putInScope(Key.get(ChannelFactory.class, ServerFactory.class),
          config.getServerChannelFactory());

      ProtocolModel model;
      try {
        holder.enterScope();
        config.scopedInit();
        model = protocolModelProvider.get();
      } catch (Exception e) {
        throw new ProvisionException("Exception while calling scopedInit() on config: " + config, e);
      } finally {
        holder.exitScope();
      }

      scopes.add(model);
    }
    return scopes.build();
  }
}

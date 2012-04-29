package edu.cornell.jnutella.gnutella.modules;

import com.google.inject.Singleton;

import edu.cornell.jnutella.gnutella.modules.handshake.HeadersModule;
import edu.cornell.jnutella.gnutella.modules.ping.AdvancedPongCache;
import edu.cornell.jnutella.gnutella.modules.ping.MaxPongsSent;
import edu.cornell.jnutella.gnutella.modules.ping.PingModule;
import edu.cornell.jnutella.gnutella.modules.ping.PongExpireTime;
import edu.cornell.jnutella.plugin.PluginGuiceModule;

public class ModulesGuiceModule extends PluginGuiceModule {

  @Override
  protected void configure() {

    addGnutellaModuleInSessionScope(PingModule.class);
    addGnutellaModuleInSessionScope(HeadersModule.class);
    
    bind(AdvancedPongCache.class).in(Singleton.class);

    bindConstant().annotatedWith(PongExpireTime.class).to(3 * 1000);
    bindConstant().annotatedWith(MaxPongsSent.class).to(10);
  }
}

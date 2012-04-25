package edu.cornell.jnutella.gnutella.modules;

import com.google.inject.Singleton;

import edu.cornell.jnutella.gnutella.modules.handshake.HeadersModule;
import edu.cornell.jnutella.gnutella.modules.ping.PRSPongCache;
import edu.cornell.jnutella.gnutella.modules.ping.PingModule;
import edu.cornell.jnutella.gnutella.modules.ping.PongCache;
import edu.cornell.jnutella.gnutella.modules.ping.PongCacheThreshold;
import edu.cornell.jnutella.gnutella.modules.ping.PongCacheTimout;
import edu.cornell.jnutella.plugin.PluginGuiceModule;

public class ModulesGuiceModule extends PluginGuiceModule {

  @Override
  protected void configure() {

    addGnutellaModuleInSessionScope(PingModule.class);
    addGnutellaModuleInSessionScope(HeadersModule.class);

    bind(PongCache.class).to(PRSPongCache.class).in(Singleton.class);
    
    bindConstant().annotatedWith(PongCacheTimout.class).to(60*1000);
    bindConstant().annotatedWith(PongCacheThreshold.class).to(20);
    
  }

}

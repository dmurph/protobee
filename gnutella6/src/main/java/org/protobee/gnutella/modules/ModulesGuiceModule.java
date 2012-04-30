package org.protobee.gnutella.modules;

import org.protobee.gnutella.modules.handshake.HeadersModule;
import org.protobee.gnutella.modules.ping.AdvancedPongCache;
import org.protobee.gnutella.modules.ping.MaxPongsSent;
import org.protobee.gnutella.modules.ping.PingModule;
import org.protobee.gnutella.modules.ping.PongExpireTime;
import org.protobee.plugin.GnutellaPluginGuiceModule;

import com.google.inject.Singleton;


public class ModulesGuiceModule extends GnutellaPluginGuiceModule {

  @Override
  protected void configure() {

    addGnutellaModuleInSessionScope(PingModule.class);
    addGnutellaModuleInSessionScope(HeadersModule.class);
    
    bind(AdvancedPongCache.class).in(Singleton.class);

    bindConstant().annotatedWith(PongExpireTime.class).to(3 * 1000);
    bindConstant().annotatedWith(MaxPongsSent.class).to(10);
  }
}

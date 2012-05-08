package org.protobee.gnutella.routing;

import org.protobee.gnutella.routing.managers.CoreRoutingTableManager;
import org.protobee.gnutella.routing.managers.PingRoutingTableManager;
import org.protobee.gnutella.routing.managers.PushRoutingTableManager;
import org.protobee.gnutella.routing.managers.QueryRoutingTableManager;
import org.protobee.gnutella.routing.tables.CoreRoutingTable;
import org.protobee.gnutella.routing.tables.GUIDRoutingTable;
import org.protobee.gnutella.routing.tables.QueryGUIDRoutingTable;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;


public class RoutingGuiceModule extends AbstractModule {

  @Override
  protected void configure() {

    install(new FactoryModuleBuilder().build(CoreRoutingTable.Factory.class));
    install(new FactoryModuleBuilder().build(GUIDRoutingTable.Factory.class));
    install(new FactoryModuleBuilder().build(QueryGUIDRoutingTable.Factory.class));

    bind(CoreRoutingTableManager.class).in(Singleton.class);
    bind(PingRoutingTableManager.class).in(Singleton.class);
    bind(PushRoutingTableManager.class).in(Singleton.class);
    bind(QueryRoutingTableManager.class).in(Singleton.class);
    
  }
}

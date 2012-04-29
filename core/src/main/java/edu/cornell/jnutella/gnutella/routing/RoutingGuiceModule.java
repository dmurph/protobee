package edu.cornell.jnutella.gnutella.routing;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import edu.cornell.jnutella.gnutella.routing.tables.CoreRoutingTable;
import edu.cornell.jnutella.gnutella.routing.tables.GUIDRoutingTable;
import edu.cornell.jnutella.gnutella.routing.tables.QueryGUIDRoutingTable;

public class RoutingGuiceModule extends AbstractModule {

  @Override
  protected void configure() {

    install(new FactoryModuleBuilder().build(CoreRoutingTable.Factory.class));
    install(new FactoryModuleBuilder().build(GUIDRoutingTable.Factory.class));
    install(new FactoryModuleBuilder().build(QueryGUIDRoutingTable.Factory.class));

  }
}

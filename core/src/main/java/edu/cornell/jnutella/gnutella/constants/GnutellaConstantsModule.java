package edu.cornell.jnutella.gnutella.constants;

import com.google.inject.AbstractModule;

public class GnutellaConstantsModule extends AbstractModule{

  @Override
  protected void configure() {
    bindConstant().annotatedWith(MaxHops.class).to(6);
    bindConstant().annotatedWith(MaxTTL.class).to(6);
    bindConstant().annotatedWith(MaxConnections.class).to(12);
  }
}

package edu.cornell.jnutella.protocol;

import com.google.inject.AbstractModule;

import edu.cornell.jnutella.guice.SessionScope;
import edu.cornell.jnutella.protocol.headers.CompatabilityHeaderMerger;

public class ProtocolGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(CompatabilityHeaderMerger.class).in(SessionScope.class);
  }

}

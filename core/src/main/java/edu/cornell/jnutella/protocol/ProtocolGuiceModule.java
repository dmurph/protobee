package edu.cornell.jnutella.protocol;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import edu.cornell.jnutella.protocol.headers.CompatabilityHeaderMerger;
import edu.cornell.jnutella.protocol.session.HandshakeInterruptor;
import edu.cornell.jnutella.protocol.session.HandshakeInterruptorImpl;
import edu.cornell.jnutella.protocol.session.ProtocolSessionBootstrapper;
import edu.cornell.jnutella.protocol.session.SessionDownstreamHandshaker;
import edu.cornell.jnutella.protocol.session.SessionUpstreamHandshaker;

public class ProtocolGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(CompatabilityHeaderMerger.Factory.class));
    install(new FactoryModuleBuilder().build(SessionUpstreamHandshaker.Factory.class));
    install(new FactoryModuleBuilder().build(SessionDownstreamHandshaker.Factory.class));
    install(new FactoryModuleBuilder().build(ProtocolSessionBootstrapper.Factory.class));

    bind(HandshakeInterruptor.class).to(HandshakeInterruptorImpl.class);
  }

}

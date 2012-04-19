package edu.cornell.jnutella.protocol;

import org.jboss.netty.channel.Channel;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import edu.cornell.jnutella.guice.PrescopedProvider;
import edu.cornell.jnutella.guice.SessionScope;
import edu.cornell.jnutella.protocol.headers.CompatabilityHeaderMerger;
import edu.cornell.jnutella.protocol.session.HandshakeInterruptor;
import edu.cornell.jnutella.protocol.session.HandshakeInterruptorImpl;
import edu.cornell.jnutella.protocol.session.ProtocolSessionBootstrapper;
import edu.cornell.jnutella.protocol.session.SessionDownstreamHandshaker;
import edu.cornell.jnutella.protocol.session.SessionModel;
import edu.cornell.jnutella.protocol.session.SessionUpstreamHandshaker;

public class ProtocolGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(ProtocolSessionBootstrapper.Factory.class));

    bind(HandshakeInterruptor.class).to(HandshakeInterruptorImpl.class);

    bind(SessionUpstreamHandshaker.class).in(SessionScope.class);
    bind(SessionDownstreamHandshaker.class).in(SessionScope.class);

    bind(EventBus.class).toProvider(new PrescopedProvider<EventBus>()).in(SessionScope.class);
    bind(SessionModel.class).toProvider(new PrescopedProvider<SessionModel>()).in(
        SessionScope.class);
    bind(Protocol.class).toProvider(new PrescopedProvider<Protocol>()).in(SessionScope.class);
    bind(Channel.class).toProvider(new PrescopedProvider<Channel>()).in(SessionScope.class);
    bind(ProtocolConfig.class).toProvider(new PrescopedProvider<ProtocolConfig>()).in(
        SessionScope.class);
    bind(ProtocolSessionBootstrapper.class).toProvider(
        new PrescopedProvider<ProtocolSessionBootstrapper>()).in(SessionScope.class);

    bind(CompatabilityHeaderMerger.class).in(SessionScope.class);
  }

}

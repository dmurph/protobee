package edu.cornell.jnutella.session;

import org.jboss.netty.channel.Channel;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import edu.cornell.jnutella.guice.PrescopedProvider;
import edu.cornell.jnutella.guice.SessionScope;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.ProtocolConfig;

public class SessionGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(ProtocolSessionBootstrapper.Factory.class));

    bind(HandshakeInterruptor.class).to(HandshakeInterruptorImpl.class);

    bind(SessionManager.class).in(Singleton.class);
    
    bind(SessionUpstreamHandshaker.class).in(SessionScope.class);
    bind(SessionDownstreamHandshaker.class).in(SessionScope.class);

    bind(ProtocolSessionModel.class).toProvider(new PrescopedProvider<ProtocolSessionModel>()).in(
        SessionScope.class);

    bind(EventBus.class).toProvider(new PrescopedProvider<EventBus>()).in(SessionScope.class);
    bind(SessionModel.class).in(SessionScope.class);
    bind(Protocol.class).toProvider(new PrescopedProvider<Protocol>()).in(SessionScope.class);
    bind(Channel.class).toProvider(new PrescopedProvider<Channel>()).in(SessionScope.class);
    bind(ProtocolConfig.class).toProvider(new PrescopedProvider<ProtocolConfig>()).in(
        SessionScope.class);
    bind(ProtocolSessionBootstrapper.class).toProvider(
        new PrescopedProvider<ProtocolSessionBootstrapper>()).in(SessionScope.class);
  }

}

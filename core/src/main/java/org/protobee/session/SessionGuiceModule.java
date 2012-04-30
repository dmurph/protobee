package org.protobee.session;

import org.jboss.netty.channel.Channel;
import org.protobee.guice.PrescopedProvider;
import org.protobee.guice.SessionScope;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolConfig;
import org.protobee.session.handshake.HandshakeInterruptor;
import org.protobee.session.handshake.HandshakeInterruptorImpl;
import org.protobee.session.handshake.SessionDownstreamHandshaker;
import org.protobee.session.handshake.SessionUpstreamHandshaker;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;


public class SessionGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(ProtocolSessionBootstrapper.Factory.class));

    bind(HandshakeInterruptor.class).to(HandshakeInterruptorImpl.class);

    bind(SessionManager.class).to(SessionManagerImpl.class).in(Singleton.class);
    
    bind(SessionUpstreamHandshaker.class).in(SessionScope.class);
    bind(SessionDownstreamHandshaker.class).in(SessionScope.class);

    bind(ProtocolModulesHolder.class).toProvider(new PrescopedProvider<ProtocolModulesHolder>()).in(
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

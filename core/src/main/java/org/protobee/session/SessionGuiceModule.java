package org.protobee.session;

import java.util.Set;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.protobee.guice.PrescopedProvider;
import org.protobee.guice.SessionScope;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolConfig;
import org.protobee.session.handshake.DefaultHandshakeHandlersProvider;
import org.protobee.session.handshake.HandshakeHandlers;
import org.protobee.session.handshake.HandshakeHttpMessageDecoder;
import org.protobee.session.handshake.HandshakeHttpMessageEncoder;
import org.protobee.session.handshake.HandshakeInterruptor;
import org.protobee.session.handshake.HandshakeInterruptorImpl;
import org.protobee.session.handshake.HandshakeStateBootstrapper;
import org.protobee.session.handshake.SessionDownstreamHandshaker;
import org.protobee.session.handshake.SessionUpstreamHandshaker;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

public class SessionGuiceModule extends AbstractModule {

  @Override
  protected void configure() {

    bind(HandshakeInterruptor.class).to(HandshakeInterruptorImpl.class);

    bind(SessionManager.class).to(SessionManagerImpl.class).in(Singleton.class);

    // handlers
    bindHandshakeHandlers();

    bind(ProtocolSessionBootstrapper.class).in(SessionScope.class);

    bind(HandshakeStateBootstrapper.class).in(Singleton.class);

    bind(ProtocolModulesHolder.class).toProvider(new PrescopedProvider<ProtocolModulesHolder>())
        .in(SessionScope.class);

    bind(EventBus.class).toProvider(new PrescopedProvider<EventBus>()).in(SessionScope.class);
    bind(SessionModel.class).in(SessionScope.class);
    bind(Protocol.class).toProvider(new PrescopedProvider<Protocol>()).in(SessionScope.class);
    bind(Channel.class).toProvider(new PrescopedProvider<Channel>()).in(SessionScope.class);
    bind(ProtocolConfig.class).toProvider(new PrescopedProvider<ProtocolConfig>()).in(
        SessionScope.class);
  }

  private void bindHandshakeHandlers() {

    bind(SessionUpstreamHandshaker.class).in(SessionScope.class);
    bind(SessionDownstreamHandshaker.class).in(SessionScope.class);
    bind(HandshakeHttpMessageDecoder.class).in(SessionScope.class);
    bind(HandshakeHttpMessageEncoder.class).in(SessionScope.class);

    bind(new TypeLiteral<Set<ChannelHandler>>() {}).annotatedWith(HandshakeHandlers.class)
        .toProvider(DefaultHandshakeHandlersProvider.class).in(SessionScope.class);
  }

}

package edu.cornell.jnutella.network;

import java.net.SocketAddress;

import javax.annotation.Nullable;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.guice.JnutellaScopes;
import edu.cornell.jnutella.identity.ProtocolIdentityModel;
import edu.cornell.jnutella.modules.ProtocolModule;
import edu.cornell.jnutella.protocol.ProtocolConfig;
import edu.cornell.jnutella.protocol.session.ProtocolSessionBootstrapper;
import edu.cornell.jnutella.protocol.session.SessionDownstreamHandshaker;
import edu.cornell.jnutella.protocol.session.SessionModel;
import edu.cornell.jnutella.protocol.session.SessionModelFactory;
import edu.cornell.jnutella.protocol.session.SessionUpstreamHandshaker;

/**
 * Needs to be threadsafe, not in any scope.
 */
@Singleton
public class HandshakeStateBootstrapper {

  @InjectLogger
  private Logger log;
  private final Provider<HandshakeHttpMessageDecoder> decoderFactory;
  private final Provider<HandshakeHttpMessageEncoder> encoderFactory;
  private final Provider<SessionUpstreamHandshaker> upShakerProvider;
  private final Provider<SessionDownstreamHandshaker> downShakerProvider;
  private final ProtocolSessionBootstrapper.Factory protocolBootstrapperFactory;
  private final Provider<SessionModelFactory> sessionFactory;

  private final Provider<EventBus> eventBus;

  @Inject
  public HandshakeStateBootstrapper(Provider<HandshakeHttpMessageDecoder> decoderFactory,
      Provider<HandshakeHttpMessageEncoder> encoderFactory,
      Provider<SessionUpstreamHandshaker> upShakerProvider,
      Provider<SessionDownstreamHandshaker> downShakerProvider,
      ProtocolSessionBootstrapper.Factory protocolBootstrapperFactory, Provider<EventBus> eventBus,
      Provider<SessionModelFactory> sessionFactory) {
    this.decoderFactory = decoderFactory;
    this.encoderFactory = encoderFactory;
    this.upShakerProvider = upShakerProvider;
    this.downShakerProvider = downShakerProvider;
    this.protocolBootstrapperFactory = protocolBootstrapperFactory;
    this.eventBus = eventBus;
    this.sessionFactory = sessionFactory;
  }

  /**
   * Precondition: we are in the respective identity scope
   * 
   * @param protocolConfig
   * @param identity
   * @param remoteAddress
   * @param channel
   * @return
   */
  public ChannelHandler[] bootstrapSession(ProtocolConfig protocolConfig,
      ProtocolIdentityModel identityModel, SocketAddress remoteAddress, @Nullable Channel channel) {
    Preconditions.checkNotNull(protocolConfig);
    Preconditions.checkNotNull(identityModel);
    Preconditions.checkNotNull(remoteAddress);
    Preconditions.checkState(JnutellaScopes.isInIdentityScope(), "Need to be in identity scope");

    // create session
    SessionModel session = sessionFactory.get().create(protocolConfig, remoteAddress.toString());
    if (channel != null) {
      session.addObjectToScope(Key.get(Channel.class), channel);
    }
    session.enterScope();

    identityModel.setCurrentSessionModel(session);

    // register modules
    EventBus bus = eventBus.get();
    for (ProtocolModule module : session.getModules()) {
      bus.register(module);
    }

    // create handlers
    ChannelHandler[] handlers = new ChannelHandler[4];

    // create protocol bootstrap
    ProtocolSessionBootstrapper protocolBootstrap = protocolBootstrapperFactory.create(handlers);
    session.addObjectToScope(Key.get(ProtocolSessionBootstrapper.class), protocolBootstrap);

    handlers[0] = decoderFactory.get();
    handlers[1] = encoderFactory.get();
    handlers[2] = upShakerProvider.get();
    handlers[3] = downShakerProvider.get();

    session.exitScope();

    return handlers;
  }
}

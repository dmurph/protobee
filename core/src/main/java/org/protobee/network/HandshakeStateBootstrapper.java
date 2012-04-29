package org.protobee.network;

import java.util.Set;

import javax.annotation.Nullable;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.protobee.annotation.InjectLogger;
import org.protobee.identity.NetworkIdentity;
import org.protobee.modules.ProtocolModule;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolConfig;
import org.protobee.session.ProtocolSessionBootstrapper;
import org.protobee.session.ProtocolSessionModel;
import org.protobee.session.SessionDownstreamHandshaker;
import org.protobee.session.SessionModel;
import org.protobee.session.SessionModelFactory;
import org.protobee.session.SessionUpstreamHandshaker;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Singleton;


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
  private final Provider<ProtocolSessionModel> protocolSession;

  private final Provider<LoggingHandler> loggingHandler;
  private final Provider<CleanupOnDisconnectHandler> cleanupHandler;

  private final Provider<EventBus> eventBus;

  @Inject
  public HandshakeStateBootstrapper(Provider<HandshakeHttpMessageDecoder> decoderFactory,
      Provider<HandshakeHttpMessageEncoder> encoderFactory,
      Provider<SessionUpstreamHandshaker> upShakerProvider,
      Provider<SessionDownstreamHandshaker> downShakerProvider,
      ProtocolSessionBootstrapper.Factory protocolBootstrapperFactory, Provider<EventBus> eventBus,
      Provider<SessionModelFactory> sessionFactory, Provider<ProtocolSessionModel> protocolSession,
      Provider<LoggingHandler> loggingHandler, Provider<CleanupOnDisconnectHandler> cleanupHandler) {
    this.decoderFactory = decoderFactory;
    this.encoderFactory = encoderFactory;
    this.upShakerProvider = upShakerProvider;
    this.downShakerProvider = downShakerProvider;
    this.protocolBootstrapperFactory = protocolBootstrapperFactory;
    this.eventBus = eventBus;
    this.sessionFactory = sessionFactory;
    this.protocolSession = protocolSession;
    this.loggingHandler = loggingHandler;
    this.cleanupHandler = cleanupHandler;
  }

  /**
   * Preconditions: not in any scope, identity already has sending and listening addresses set
   * 
   * 
   * @param protocolConfig
   * @param identity
   * @param remoteAddress
   * @param channel
   * @param pipeline
   * @return
   */
  public void bootstrapSession(ProtocolConfig protocolConfig, NetworkIdentity identity, @Nullable Channel channel, ChannelPipeline pipeline) {
    Preconditions.checkNotNull(protocolConfig);
    Preconditions.checkNotNull(identity);

    Protocol protocol = protocolConfig.get();

    SessionModel session = null;
    try {
      identity.enterScope();
      // create session
      session = sessionFactory.get().create(protocolConfig, identity.getSendingAddress(protocol).toString());
      if (channel != null) {
        session.addObjectToScope(Key.get(Channel.class), channel);
      }

      session.enterScope();

      identity.registerNewSession(protocol, session);

      ProtocolSessionModel protocolSessionModel = protocolSession.get();

      Set<ProtocolModule> modules = protocolSessionModel.getMutableModules();
      if (modules.size() == 0) {
        log.info("No protocol modules for protocol: " + protocol);
      } else {
        log.info(modules.size() + " modules available for protocol session " + protocol);
        log.debug(modules.toString());
        // register modules
        EventBus bus = eventBus.get();
        for (ProtocolModule module : protocolSessionModel.getMutableModules()) {
          bus.register(module);
        }
      }

      // create handlers
      ChannelHandler[] handlers = new ChannelHandler[4];

      // create protocol bootstrap
      ProtocolSessionBootstrapper protocolBootstrap = protocolBootstrapperFactory.create(handlers);
      session.addObjectToScope(Key.get(ProtocolSessionBootstrapper.class), protocolBootstrap);

      handlers[0] = encoderFactory.get();
      handlers[1] = decoderFactory.get();
      handlers[2] = downShakerProvider.get();
      handlers[3] = upShakerProvider.get();

      for (ChannelHandler handler : handlers) {
        pipeline.addLast(handler.toString(), handler);
      }
      CleanupOnDisconnectHandler cleanup = cleanupHandler.get();
      LoggingHandler logging = loggingHandler.get();
      pipeline.addLast(cleanup.toString(), cleanup);
      pipeline.addLast(logging.toString(), logging);

    } finally {
      if (session != null) {
        session.exitScope();
      }
      identity.exitScope();
    }
  }
}

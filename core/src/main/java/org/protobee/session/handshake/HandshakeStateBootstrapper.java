package org.protobee.session.handshake;

import java.util.Set;

import javax.annotation.Nullable;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.protobee.annotation.InjectLogger;
import org.protobee.identity.NetworkIdentity;
import org.protobee.modules.ProtocolModule;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolModel;
import org.protobee.session.ProtocolSessionBootstrapper;
import org.protobee.session.SessionModel;
import org.protobee.session.SessionModelFactory;
import org.protobee.session.SessionProtocolModules;
import org.protobee.session.SessionState;
import org.protobee.util.ProtocolUtils;
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
  private final Provider<ProtocolSessionBootstrapper> protocolBootstrapperFactory;
  private final Provider<SessionModelFactory> sessionFactory;
  private final Provider<SessionProtocolModules> protocolSession;

  private final Provider<Set<ChannelHandler>> handshakeHandlersProvider;

  private final Provider<EventBus> eventBus;

  @Inject
  public HandshakeStateBootstrapper(
      Provider<ProtocolSessionBootstrapper> protocolBootstrapperFactory,
      Provider<EventBus> eventBus, Provider<SessionModelFactory> sessionFactory,
      Provider<SessionProtocolModules> protocolSession,
      @HandshakeHandlers Provider<Set<ChannelHandler>> handshakeHandlersProvider,
      Provider<Protocol> protocolProvider) {
    this.protocolBootstrapperFactory = protocolBootstrapperFactory;
    this.eventBus = eventBus;
    this.sessionFactory = sessionFactory;
    this.protocolSession = protocolSession;
    this.handshakeHandlersProvider = handshakeHandlersProvider;
  }

  /**
   * Preconditions: identity already has sending and listening addresses set
   */
  public void bootstrapSession(ProtocolModel protocolModel, NetworkIdentity identity,
      @Nullable Channel channel, ChannelPipeline pipeline) {
    Preconditions.checkNotNull(identity);
    Protocol protocol = protocolModel.getProtocol();

    log.debug("Bootstrapping session for protocol " + protocolModel + " with identity " + identity);
    SessionModel session = null;
    try {
      protocolModel.enterScope();
      identity.enterScope();
      // create session
      session =
          sessionFactory.get().create(
              protocol.name() + "-" + identity.getSendingAddress(protocol).toString());
      if (channel != null) {
        session.getScope().putInScope(Key.get(Channel.class), channel);
      }

      session.setSessionState(SessionState.HANDSHAKE_0);

      session.enterScope();

      identity.registerNewSession(protocol, session);

      SessionProtocolModules protocolSessionModel = protocolSession.get();

      Set<ProtocolModule> modules = protocolSessionModel.getMutableModules();
      if (modules.size() == 0) {
        log.info("No protocol modules for protocol: " + ProtocolUtils.toString(protocol));
      } else {
        log.info(modules.size() + " modules available for protocol session "
            + ProtocolUtils.toString(protocol));
        log.debug(modules.toString());
        // register modules
        EventBus bus = eventBus.get();
        for (ProtocolModule module : protocolSessionModel.getMutableModules()) {
          bus.register(module);
        }
      }

      // create protocol bootstrap
      session.getScope().putInScope(Key.get(ProtocolSessionBootstrapper.class),
          protocolBootstrapperFactory.get());

      Set<ChannelHandler> handlers = handshakeHandlersProvider.get();

      for (ChannelHandler handler : handlers) {
        pipeline.addLast(handler.toString(), handler);
      }

    } finally {
      if (session != null) {
        session.exitScope();
      }
      identity.exitScope();
      protocolModel.exitScope();
    }
  }
}

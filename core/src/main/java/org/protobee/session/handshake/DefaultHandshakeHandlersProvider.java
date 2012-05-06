package org.protobee.session.handshake;

import java.util.Set;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.logging.InternalLogLevel;
import org.protobee.network.handlers.CleanupOnDisconnectHandler;
import org.protobee.network.handlers.CloseOnExceptionHandler;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Preconditions: we must be corresponding protocol, identity, session scope, and our channel
 * handlers must be in session scope
 * 
 * @author Daniel
 */
public class DefaultHandshakeHandlersProvider implements Provider<Set<ChannelHandler>> {

  private final Provider<HandshakeHttpMessageDecoder> decoderFactory;
  private final Provider<HandshakeHttpMessageEncoder> encoderFactory;
  private final Provider<SessionUpstreamHandshaker> upShakerProvider;
  private final Provider<SessionDownstreamHandshaker> downShakerProvider;
  private final Provider<CleanupOnDisconnectHandler> cleanupHandler;
  private final Provider<CloseOnExceptionHandler> closeHandler;

  @Inject
  public DefaultHandshakeHandlersProvider(Provider<HandshakeHttpMessageDecoder> decoderFactory,
      Provider<HandshakeHttpMessageEncoder> encoderFactory,
      Provider<SessionUpstreamHandshaker> upShakerProvider,
      Provider<SessionDownstreamHandshaker> downShakerProvider,
      Provider<CleanupOnDisconnectHandler> cleanupHandler,
      Provider<CloseOnExceptionHandler> closeHandler) {
    this.decoderFactory = decoderFactory;
    this.encoderFactory = encoderFactory;
    this.upShakerProvider = upShakerProvider;
    this.downShakerProvider = downShakerProvider;
    this.cleanupHandler = cleanupHandler;
    this.closeHandler = closeHandler;
  }

  @Override
  public Set<ChannelHandler> get() {
    return ImmutableSet.<ChannelHandler>of(encoderFactory.get(), decoderFactory.get(),new LoggingHandler("HandshakeLogger", InternalLogLevel.DEBUG, false),
        downShakerProvider.get(), upShakerProvider.get(), closeHandler.get(),
        cleanupHandler.get());
  }
}

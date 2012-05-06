package org.protobee.network;

import java.net.SocketAddress;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.DefaultChannelFuture;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.protobee.annotation.InjectLogger;
import org.protobee.identity.NetworkIdentity;
import org.protobee.protocol.HandshakeFuture;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolModel;
import org.protobee.session.SessionModel;
import org.protobee.session.SessionState;
import org.protobee.util.Descoper;
import org.protobee.util.ProtocolUtils;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class ProtobeeMessageWriterImpl implements ProtobeeMessageWriter {

  @InjectLogger
  private Logger log;
  private final Provider<Channel> channelProvider;
  private final Provider<NetworkIdentity> identityProvider;
  private final Provider<SessionModel> sessionProvider;
  private final Provider<ProtocolModel> protocolProvider;
  private final ConnectionCreator networkManager;
  private final Provider<ChannelFuture> handshakeFutureProvider;
  private final Provider<Descoper> descoperProvider;

  @Inject
  public ProtobeeMessageWriterImpl(Provider<Channel> channelProvider,
      Provider<NetworkIdentity> identityProvider, Provider<SessionModel> sessionProvider,
      ConnectionCreator networkManager, Provider<ProtocolModel> protocolProvider,
      @HandshakeFuture Provider<ChannelFuture> handshakeFutureProvider,
      Provider<Descoper> descoperProvider) {
    this.channelProvider = channelProvider;
    this.identityProvider = identityProvider;
    this.sessionProvider = sessionProvider;
    this.networkManager = networkManager;
    this.protocolProvider = protocolProvider;
    this.handshakeFutureProvider = handshakeFutureProvider;
    this.descoperProvider = descoperProvider;
  }

  @Override
  public ChannelFuture write(Object message) {
    Preconditions.checkNotNull(message);
    Channel channel = channelProvider.get();
    SessionModel session = sessionProvider.get();
    SocketAddress address =
        session.getIdentity().getListeningAddress(session.getProtocol().getProtocol());
    return write(channel, message, address);
  }

  @Override
  public ChannelFuture write(Object message, HandshakeOptions handshakeOptions) {
    return write(message, ConnectionOptions.EXIT_IF_NO_CONNECTION, null, null, handshakeOptions);
  }

  @Override
  public ChannelFuture write(Object message, HttpMethod method, String uri) {
    return write(message, ConnectionOptions.CAN_CREATE_CONNECTION, method, uri,
        HandshakeOptions.WAIT_FOR_HANDSHAKE);
  }

  @Override
  public ChannelFuture write(final Object message, ConnectionOptions connectionOptions,
      HttpMethod method, String uri, HandshakeOptions handshakeOptions) {
    Preconditions.checkNotNull(message);
    Preconditions.checkNotNull(connectionOptions);
    Preconditions.checkNotNull(handshakeOptions);
    if (connectionOptions == ConnectionOptions.CAN_CREATE_CONNECTION) {
      Preconditions.checkNotNull(method);
      Preconditions.checkNotNull(uri);
    }

    ProtocolModel protocolModel = protocolProvider.get();
    final Protocol protocol = protocolModel.getProtocol();
    final NetworkIdentity identity = identityProvider.get();

    ChannelFuture messageFuture = null;
    if (identity.hasCurrentSession(protocol)) {
      log.debug("Current session of " + ProtocolUtils.toString(protocol)
          + " already running for identity '" + identity + "', dispatching in that session.");
      SessionModel session = identity.getCurrentSession(protocol);

      Channel channel = null;
      try {
        session.enterScope();
        channel = channelProvider.get();

        if (session.getSessionState() == SessionState.MESSAGES) {
          messageFuture = write(channel, message, identity.getListeningAddress(protocol));
        } else {
          messageFuture = new DefaultChannelFuture(channel, false);
          if (handshakeOptions == HandshakeOptions.EXIT_IF_HANDSHAKING) {
            log.info("In handshake state, exiting");
            // TODO should we have a throwable here?
            messageFuture.setFailure(null);
            session.exitScope();
            return messageFuture;
          }
          ChannelFuture handshakeFuture = handshakeFutureProvider.get();

          final ChannelFuture finalFuture = messageFuture;
          final Channel finalChannel = channel;
          handshakeFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
              if (!future.isSuccess()) {
                finalFuture.setFailure(future.getCause());
                return;
              }

              if (future.isSuccess()) {
                ChannelFuture writeFuture =
                    write(finalChannel, message, identity.getListeningAddress(protocol));
                writeFuture.addListener(new ChannelFutureListener() {

                  @Override
                  public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                      finalFuture.setSuccess();
                    } else {
                      finalFuture.setFailure(future.getCause());
                    }
                  }
                });
              }
            }
          });
        }
      } catch (Exception e) {
        log.error("Exception while sending " + message + " to " + identity, e);
        if (messageFuture == null) {
          messageFuture = new DefaultChannelFuture(channel, false);
        }
        messageFuture.setFailure(e);
      } finally {
        session.exitScope();
      }

      return messageFuture;
    }

    if (connectionOptions == ConnectionOptions.EXIT_IF_NO_CONNECTION) {
      log.info("No current connection, exiting");
      // TODO should we have a throwable here?
      messageFuture = new DefaultChannelFuture(null, false);
      messageFuture.setFailure(null);
      return messageFuture;
    }

    // we have to make a new connection
    SocketAddress address = identity.getListeningAddress(protocol);
    if (address == null) {
      log.error("we have no address to connect to for " + protocol + " at " + identity);
      return null;
    }

    Descoper descoper = descoperProvider.get();
    descoper.descope();
    ChannelFuture handshakeFuture = networkManager.connect(null, address, method, uri);
    descoper.rescope();

    final Channel channel = handshakeFuture.getChannel();

    messageFuture = new DefaultChannelFuture(channel, false);

    final ChannelFuture finalFuture = messageFuture;
    handshakeFuture.addListener(new ChannelFutureListener() {

      @Override
      public void operationComplete(ChannelFuture future) throws Exception {
        if (!future.isSuccess()) {
          finalFuture.setFailure(future.getCause());
          return;
        }

        try {
          if (future.isSuccess()) {
            ChannelFuture writeFuture =
                write(channel, message, identity.getListeningAddress(protocol));
            writeFuture.addListener(new ChannelFutureListener() {

              @Override
              public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                  finalFuture.setSuccess();
                } else {
                  finalFuture.setFailure(future.getCause());
                }
              }
            });
          }
        } catch (Exception e) {
          log.error("Exception while writing " + message + " to " + identity, e);
          finalFuture.setFailure(e);
        }
      }
    });
    return messageFuture;
  }

  protected ChannelFuture write(Channel channel, Object message, SocketAddress address) {
    Preconditions.checkNotNull(channel, "Channel address is null");
    Preconditions.checkNotNull(message, "Message is null");
    Preconditions.checkNotNull(address, "Remote address is null");
    log.debug("Writing object [" + message + "] to address " + address);
    Descoper descoper = descoperProvider.get();
    descoper.descope();
    ChannelFuture future = Channels.write(channel, message, address);
    descoper.rescope();
    return future;
  }
}

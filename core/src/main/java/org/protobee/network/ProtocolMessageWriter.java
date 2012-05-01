package org.protobee.network;

import java.net.SocketAddress;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.DefaultChannelFuture;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.protobee.annotation.InjectLogger;
import org.protobee.guice.SessionScope;
import org.protobee.identity.NetworkIdentity;
import org.protobee.protocol.HandshakeFuture;
import org.protobee.protocol.Protocol;
import org.protobee.session.SessionModel;
import org.protobee.session.SessionState;
import org.protobee.util.Descoper;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;


/**
 * <p>
 * Helper class for writing message to a protocol connection. If the handshake is not complete yet,
 * the message will be sent after the handshake is complete. Similarly, if there in no connection, a
 * new one is made.
 * </p>
 * <p>
 * Operations on this object correspond to the session it resides in.
 * </p>
 * <p>
 * Preconditions for injection: needs to be in identity and session scope
 * </p>
 * 
 * @author Daniel
 */
@SessionScope
public class ProtocolMessageWriter {

  public static enum ConnectionOptions {
    CAN_CREATE_CONNECTION, EXIT_IF_NO_CONNECTION
  }

  public static enum HandshakeOptions {
    WAIT_FOR_HANDSHAKE, EXIT_IF_HANDSHAKING
  }

  @InjectLogger
  private Logger log;
  private final Channel channel;
  private final NetworkIdentity myIdentity;
  private final Descoper descoper;
  private final ConnectionCreator networkManager;
  private final Protocol protocol;
  private final Provider<Channel> channelProvider;
  private final Provider<ChannelFuture> handshakeFutureProvider;

  @Inject
  public ProtocolMessageWriter(Channel channel, NetworkIdentity myIdentity, Descoper descoper,
      ConnectionCreator networkManager, Protocol protocol, Provider<Channel> channelProvider,
      @HandshakeFuture Provider<ChannelFuture> handshakeFuture) {
    this.channel = channel;
    this.myIdentity = myIdentity;
    this.descoper = descoper;
    this.networkManager = networkManager;
    this.protocol = protocol;
    this.channelProvider = channelProvider;
    this.handshakeFutureProvider = handshakeFuture;
  }

  /**
   * Writes the object to this object's session.
   * 
   * @param message
   * @return
   */
  public ChannelFuture write(Object message) {
    Preconditions.checkNotNull(message);
    return write(channel, message, myIdentity.getListeningAddress(protocol));
  }

  /**
   * Writes the object to the given identity's session, using the the protocol of this object's
   * session. Defaults with options {@link ConnectionOptions#CAN_CREATE_CONNECTION} and
   * {@link HandshakeOptions#WAIT_FOR_HANDSHAKE}, with method of "CONNECT" and uri of ""
   * 
   * @param identity
   * @param message
   * @return
   */
  public ChannelFuture write(NetworkIdentity identity, final Object message) {
    return write(identity, message, ConnectionOptions.CAN_CREATE_CONNECTION,
        HandshakeOptions.WAIT_FOR_HANDSHAKE);
  }

  /**
   * Writes the object to the given identity's session, using the the protocol of this object's
   * session. Uses connection and handshake options. If a new connection is required, method
   * defaults to "CONNECT" and uri is ""
   * 
   * @param identity
   * @param message
   * @param connectionOptions
   * @param handshakeOptions
   * @return
   */
  public ChannelFuture write(final NetworkIdentity identity, final Object message,
      ConnectionOptions connectionOptions, HandshakeOptions handshakeOptions) {
    return write(identity, message, connectionOptions, HttpMethod.valueOf("CONNECT"), "",
        handshakeOptions);
  }

  /**
   * Writes the object to the given identity's session, using the the protocol of this object's
   * session. Uses connection and handshake options. If a new connection is required, the given
   * method and uri are used
   * 
   * @param identity
   * @param message
   * @param connectionOptions
   * @param method
   * @param uri
   * @param handshakeOptions
   * @return
   */
  public ChannelFuture write(final NetworkIdentity identity, final Object message,
      ConnectionOptions connectionOptions, HttpMethod method, String uri,
      HandshakeOptions handshakeOptions) {
    if (myIdentity == identity) {
      return write(message);
    }
    ChannelFuture messageFuture = null;
    if (identity.hasCurrentSession(protocol)) {
      log.debug("Current session already running for identity '" + identity
          + "', dispatching in that session.");
      SessionModel session = identity.getCurrentSession(protocol);

      try {
        descoper.descope();
        session.enterScope();
        final Channel channel = channelProvider.get();

        if (session.getSessionState() == SessionState.MESSAGES) {
          messageFuture = write(channel, message, identity.getListeningAddress(protocol));
        } else {
          messageFuture = new DefaultChannelFuture(channel, false);
          if (handshakeOptions == HandshakeOptions.EXIT_IF_HANDSHAKING) {
            log.info("In handshake state, exiting");
            // TODO should we have a throwable here?
            messageFuture.setFailure(null);
            session.exitScope();
            descoper.rescope();
            return messageFuture;
          }
          ChannelFuture handshakeFuture = handshakeFutureProvider.get();

          final ChannelFuture finalFuture = messageFuture;
          handshakeFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
              if (!future.isSuccess()) {
                finalFuture.setFailure(future.getCause());
                return;
              }

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
        descoper.rescope();
      }

      return messageFuture;
    }

    messageFuture = new DefaultChannelFuture(channel, false);
    if (connectionOptions == ConnectionOptions.EXIT_IF_NO_CONNECTION) {
      log.info("No current connection, exiting");
      // TODO should we have a throwable here?
      messageFuture.setFailure(null);
      return messageFuture;
    }

    // we have to make a new connection
    SocketAddress address = identity.getListeningAddress(protocol);
    if (address == null) {
      log.error("we have no address to connect to for " + protocol + " at " + identity);
      return null;
    }

    descoper.descope();
    ChannelFuture handshakeFuture = networkManager.connect(protocol, address, method, uri);
    descoper.rescope();

    final Channel channel = handshakeFuture.getChannel();

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
    log.debug("Writing object '" + message + "' to address " + address);
    return Channels.write(channel, message, address);
  }
}

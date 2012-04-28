package edu.cornell.jnutella.network;

import java.net.SocketAddress;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.DefaultChannelFuture;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.guice.SessionScope;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.protocol.HandshakeFuture;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.session.SessionModel;
import edu.cornell.jnutella.session.SessionState;
import edu.cornell.jnutella.util.Descoper;

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
    super();
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
    return write(channel, message);
  }

  /**
   * Writes the object to the given identity's session, using the the protocol of this object's
   * session. Defaults with options {@link ConnectionOptions#CAN_CREATE_CONNECTION} and
   * {@link HandshakeOptions#WAIT_FOR_HANDSHAKE}.
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
   * session. Uses connection and handshake options
   * 
   * @param identity
   * @param message
   * @param connectionOptions
   * @param handshakeOptions
   * @return
   */
  public ChannelFuture write(NetworkIdentity identity, final Object message,
      ConnectionOptions connectionOptions, HandshakeOptions handshakeOptions) {
    if (myIdentity == identity) {
      return write(message);
    }
    if (identity.hasCurrentSession(protocol)) {
      log.debug("Current session already running for identity '" + identity
          + "', dispatching in that session.");
      SessionModel session = identity.getCurrentSession(protocol);

      final ChannelFuture messageFuture;
      try {
        descoper.descope();
        session.enterScope();
        final Channel channel = channelProvider.get();

        if (session.getSessionState() == SessionState.MESSAGES) {
          messageFuture = write(channel, message);
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
          handshakeFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
              if (!future.isSuccess()) {
                messageFuture.setFailure(future.getCause());
                return;
              }

              if (future.isSuccess()) {
                ChannelFuture writeFuture = write(channel, message);
                writeFuture.addListener(new ChannelFutureListener() {

                  @Override
                  public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                      messageFuture.setSuccess();
                    } else {
                      messageFuture.setFailure(future.getCause());
                    }
                  }
                });
              }
            }
          });
        }
      } finally {
        session.exitScope();
        descoper.rescope();
      }

      return messageFuture;
    }
    final ChannelFuture messageFuture = new DefaultChannelFuture(channel, false);
    if (connectionOptions == ConnectionOptions.EXIT_IF_NO_CONNECTION) {
      log.info("No current connection, exiting");
      // TODO should we have a throwable here?
      messageFuture.setFailure(null);
      return messageFuture;
    }

    // we have to make a new connection
    SocketAddress address = identity.getAddress(protocol);
    if (address == null) {
      log.error("we have no address to connect to for " + protocol + " at " + identity);
      return null;
    }

    descoper.descope();
    ChannelFuture handshakeFuture = networkManager.connect(protocol, address);
    descoper.rescope();

    final Channel channel = handshakeFuture.getChannel();

    handshakeFuture.addListener(new ChannelFutureListener() {

      @Override
      public void operationComplete(ChannelFuture future) throws Exception {
        if (!future.isSuccess()) {
          messageFuture.setFailure(future.getCause());
          return;
        }

        if (future.isSuccess()) {
          ChannelFuture writeFuture = write(channel, message);
          writeFuture.addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
              if (future.isSuccess()) {
                messageFuture.setSuccess();
              } else {
                messageFuture.setFailure(future.getCause());
              }
            }
          });
        }
      }
    });
    return messageFuture;
  }

  protected ChannelFuture write(Channel channel, Object message) {
    log.debug("Writing object '" + message + "' to channel " + channel);
    return Channels.write(channel, message);
  }
}

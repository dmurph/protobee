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
import edu.cornell.jnutella.identity.ProtocolIdentityModel;
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
   * session. Waits for a pending handshake, and makes a new connnection if needed.
   * 
   * @param identity
   * @param message
   * @return
   */
  public ChannelFuture write(NetworkIdentity identity, final Object message) {
    if (myIdentity == identity) {
      return write(message);
    }
    ProtocolIdentityModel identityModel = identity.getModel(protocol);
    if (identityModel.hasCurrentSession()) {
      log.debug("Current session already running for identity '" + identity
          + "', dispatching in that session.");
      SessionModel session = identityModel.getCurrentSession();
      
      final ChannelFuture messageFuture;
      try {
        descoper.descope();
        session.enterScope();
        final Channel channel = channelProvider.get();

        if (session.getSessionState() == SessionState.MESSAGES) {
          messageFuture = write(channel, message);
        } else {
          messageFuture = new DefaultChannelFuture(channel, false);
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

    // we have to make a new connection
    SocketAddress address = identityModel.getNetworkAddress();
    if (address == null) {
      log.error("we have no address to connect to for " + protocol + " at " + identity);
      return null;
    }

    descoper.descope();
    ChannelFuture handshakeFuture = networkManager.connect(protocol, address);
    descoper.rescope();

    final Channel channel = handshakeFuture.getChannel();
    final ChannelFuture messageFuture = new DefaultChannelFuture(channel, false);
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

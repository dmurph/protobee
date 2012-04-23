package edu.cornell.jnutella.gnutella.session;

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
import edu.cornell.jnutella.gnutella.Gnutella;
import edu.cornell.jnutella.gnutella.messages.GnutellaMessage;
import edu.cornell.jnutella.guice.SessionScope;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.identity.ProtocolIdentityModel;
import edu.cornell.jnutella.network.NetworkManager;
import edu.cornell.jnutella.protocol.HandshakeFuture;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.session.SessionModel;
import edu.cornell.jnutella.session.SessionState;
import edu.cornell.jnutella.util.Descoper;

/**
 * Helper class for dispatching messages. Preconditions for injection: needs to be in identity and
 * session scope
 * 
 * @author Daniel
 */
@SessionScope
public class MessageDispatcher {

  @InjectLogger
  private Logger log;
  private final Channel channel;
  private final NetworkIdentity myIdentity;
  private final Descoper descoper;
  private final NetworkManager networkManager;
  private final Protocol gnutella;
  private final Provider<Channel> channelProvider;
  private final Provider<ChannelFuture> handshakeFutureProvider;

  @Inject
  public MessageDispatcher(Channel channel, NetworkIdentity myIdentity, Descoper descoper,
      NetworkManager networkManager, @Gnutella Protocol gnutella,
      Provider<Channel> channelProvider, @HandshakeFuture Provider<ChannelFuture> handshakeFuture) {
    super();
    this.channel = channel;
    this.myIdentity = myIdentity;
    this.descoper = descoper;
    this.networkManager = networkManager;
    this.gnutella = gnutella;
    this.channelProvider = channelProvider;
    this.handshakeFutureProvider = handshakeFuture;
  }

  public ChannelFuture dispatchMessage(GnutellaMessage message) {
    Preconditions.checkNotNull(message);
    return dispatchMessage(channel, message);
  }

  public ChannelFuture dispatchMessage(NetworkIdentity identity, final GnutellaMessage message) {
    if (myIdentity == identity) {
      return dispatchMessage(message);
    }
    ProtocolIdentityModel identityModel = identity.getModel(gnutella);
    if (identityModel.hasCurrentSession()) {
      log.debug("Current session already running for identity '" + identity
          + "', dispatching in that session.");
      descoper.descope();
      SessionModel session = identityModel.getCurrentSession();
      session.enterScope();
      final Channel channel = channelProvider.get();

      final ChannelFuture messageFuture;
      if (session.getSessionState() == SessionState.MESSAGES) {
        messageFuture = dispatchMessage(channel, message);
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
              ChannelFuture writeFuture = dispatchMessage(channel, message);
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
      session.exitScope();
      descoper.rescope();

      return messageFuture;

    }

    // we have to make a new connection
    SocketAddress address = identityModel.getNetworkAddress();
    if (address == null) {
      log.error("we have no address to connect to for gnutella at " + identity);
      return null;
    }
    
    descoper.descope();
    ChannelFuture handshakeFuture = networkManager.connect(gnutella, address);
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
          ChannelFuture writeFuture = dispatchMessage(channel, message);
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

  protected ChannelFuture dispatchMessage(Channel channel, GnutellaMessage message) {
    log.debug("Dispatching message '" + message + "' to channel " + channel);
    return Channels.write(channel, message);
  }
}

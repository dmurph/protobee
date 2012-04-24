package edu.cornell.jnutella.gnutella.modules.handshake;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.jboss.netty.handler.codec.http.HttpMessage;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.common.net.InetAddresses;
import com.google.inject.Inject;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.gnutella.Gnutella;
import edu.cornell.jnutella.gnutella.GnutellaIdentityModel;
import edu.cornell.jnutella.guice.SessionScope;
import edu.cornell.jnutella.guice.UserAgent;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.identity.NetworkIdentityManager;
import edu.cornell.jnutella.modules.ProtocolModule;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.session.HandshakeReceivedEvent;
import edu.cornell.jnutella.session.HandshakeSendingEvent;
import edu.cornell.jnutella.session.SessionModel;

@SessionScope
public class HeadersModule implements ProtocolModule {

  @InjectLogger
  private Logger log;
  private final String userAgent;
  private final NetworkIdentityManager identityManager;
  private final SessionModel session;
  private final Protocol gnutella;

  @Inject
  public HeadersModule(@UserAgent String userAgent, NetworkIdentityManager identityManager,
      SessionModel session, @Gnutella Protocol gnutella) {
    this.userAgent = userAgent;
    this.identityManager = identityManager;
    this.session = session;
    this.gnutella = gnutella;
  }

  @Subscribe
  public void sendingHandshake(HandshakeSendingEvent sending) {
    switch (session.getSessionState()) {
      case HANDSHAKE_0:
      case HANDSHAKE_1:
        sending.getMessage().addHeader("User-Agent", userAgent);
        SocketAddress socketAddress = sending.getContext().getChannel().getRemoteAddress();
        InetSocketAddress inetAddress = (InetSocketAddress) socketAddress;
        sending.getMessage().addHeader("Remote-IP", inetAddress.getAddress().getHostAddress());
        break;
    }

  }

  public void receivingHandshake(HandshakeReceivedEvent event) {
    switch (session.getSessionState()) {
      case HANDSHAKE_0:
      case HANDSHAKE_1:
        HttpMessage message = event.getMessage();
        if (message.containsHeader("Remote-IP")) {
          String remoteIP = message.getHeader("Remote-IP");
          InetAddress address = InetAddresses.forString(remoteIP);

          NetworkIdentity me = identityManager.getMe();
          GnutellaIdentityModel identityModel = (GnutellaIdentityModel) me.getModel(gnutella);
          InetSocketAddress socketAddress = (InetSocketAddress) identityModel.getNetworkAddress();
          if (!address.equals(socketAddress)) {
            Preconditions.checkState(socketAddress != null,
                "We're in the middle of a gnutella connection, our host address can't be null!");

            InetSocketAddress newAddress = new InetSocketAddress(address, socketAddress.getPort());
            log.info("Setting new address from Remote-IP header.  Was " + socketAddress
                + ", now we're " + newAddress);
            identityManager.setNetworkAddress(me, gnutella, newAddress);
          }
        }

        break;
    }
  }
}

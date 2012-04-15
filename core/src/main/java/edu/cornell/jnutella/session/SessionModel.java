package edu.cornell.jnutella.session;

import org.jboss.netty.channel.Channel;

import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.identity.ProtocolIdentityModel;
import edu.cornell.jnutella.protocol.Protocol;

public abstract class SessionModel {

  private final Channel channel;
  private final NetworkIdentity identity;
  private final Protocol protocol;

  public SessionModel(Channel channel, Protocol protocol, NetworkIdentity identity) {
    this.channel = channel;
    this.protocol = protocol;
    this.identity = identity;
  }

  public Channel getChannel() {
    return channel;
  }

  public Protocol getProtocol() {
    return protocol;
  }

  public NetworkIdentity getIdentity() {
    return identity;
  }
  
  public ProtocolIdentityModel getProtocolModel() {
    return identity.getModel(protocol);
  }
}

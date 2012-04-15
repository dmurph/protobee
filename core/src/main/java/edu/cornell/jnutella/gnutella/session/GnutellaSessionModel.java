package edu.cornell.jnutella.gnutella.session;

import org.jboss.netty.channel.Channel;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import edu.cornell.jnutella.gnutella.GnutellaIdentityModel;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.session.SessionModel;

public class GnutellaSessionModel extends SessionModel {

  public static interface Factory {
    GnutellaSessionModel createSessionModel(Channel channel, Protocol protocol,
        NetworkIdentity identity);
  }

  private GnutellaSessionState state;

  @AssistedInject
  public GnutellaSessionModel(@Assisted Channel channel, @Assisted Protocol protocol,
      @Assisted NetworkIdentity identity) {
    super(channel, protocol, identity);
  }

  public GnutellaSessionState getState() {
    return state;
  }

  public void setState(GnutellaSessionState state) {
    this.state = state;
  }
  
  public GnutellaIdentityModel getProtocolModel() {
    return (GnutellaIdentityModel) super.getProtocolModel();
  }
}

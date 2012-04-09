package edu.cornell.jnutella.session.gnutella;

import org.jboss.netty.channel.Channel;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.session.SessionModel;

public class GnutellaSessionModel extends SessionModel {

  private GnutellaSessionState state;

  @AssistedInject
  public GnutellaSessionModel(@Assisted Channel channel, @Assisted Protocol protocol) {
    super(channel, protocol);
  }

  public GnutellaSessionState getState() {
    return state;
  }

  public void setState(GnutellaSessionState state) {
    this.state = state;
  }
}

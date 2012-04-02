package edu.cornell.jnutella.session.gnutella;

import org.jboss.netty.channel.Channel;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import edu.cornell.jnutella.session.SessionModel;

public class GnutellaSessionModel extends SessionModel {

  private GnutellaSessionState state;

  @Inject
  public GnutellaSessionModel(@Assisted Channel channel) {
    super(channel);
  }

  public GnutellaSessionState getState() {
    return state;
  }

  public void setState(GnutellaSessionState state) {
    this.state = state;
  }

}

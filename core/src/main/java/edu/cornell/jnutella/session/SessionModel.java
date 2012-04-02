package edu.cornell.jnutella.session;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.channel.Channel;

public abstract class SessionModel {
  
  private final Channel channel;
  
  public SessionModel(Channel channel) {
    this.channel = channel;
  }
  
  public Channel getChannel() {
    return channel;
  }
}

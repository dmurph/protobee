package edu.cornell.jnutella.session;

import org.jboss.netty.channel.Channel;

import edu.cornell.jnutella.protocol.Protocol;

public abstract class SessionModel {

  private final Channel channel;
  private final Protocol protocol;

  public SessionModel(Channel channel, Protocol protocol) {
    this.channel = channel;
    this.protocol = protocol;
  }

  public Channel getChannel() {
    return channel;
  }

  public Protocol getProtocol() {
    return protocol;
  }
}

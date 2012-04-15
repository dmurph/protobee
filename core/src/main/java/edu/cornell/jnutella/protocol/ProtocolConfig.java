package edu.cornell.jnutella.protocol;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;

import edu.cornell.jnutella.identity.ProtocolIdentityModel;
import edu.cornell.jnutella.session.SessionModel;

public interface ProtocolConfig {

  SessionModel createSessionModel(Channel channel, Protocol protocol);

  Iterable<ChannelHandler> createChannelHandlers();
  
  ProtocolIdentityModel createIdentityModel();
}

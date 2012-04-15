package edu.cornell.jnutella.protocol;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;

import com.google.inject.Provider;

import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.identity.ProtocolIdentityModel;
import edu.cornell.jnutella.session.SessionModel;

public interface ProtocolConfig extends Provider<Protocol> {

  SessionModel createSessionModel(Channel channel, NetworkIdentity identity);

  Iterable<ChannelHandler> createChannelHandlers();

  ProtocolIdentityModel createIdentityModel();
}

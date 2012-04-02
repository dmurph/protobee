package edu.cornell.jnutella.network;

import org.jboss.netty.channel.ChannelHandler;

import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.session.SessionModel;

public interface ProtocolConfigProvider {

  SessionModel createSessionModel();

  ChannelHandler[] createChannelHandlers();

  Protocol getProtocol();
}

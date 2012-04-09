package edu.cornell.jnutella.protocol.config;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;

import com.google.inject.Provider;

import edu.cornell.jnutella.annotation.Gnutella;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.ProtocolConfig;
import edu.cornell.jnutella.session.SessionModel;
import edu.cornell.jnutella.session.gnutella.GnutellaSessionModel;

@Protocol(name = "GNUTELLA", version = "0.6", headerRegex = "^GNUTELLA CONNECT/0\\.6$")
public class GnutellaProtocolConfig implements ProtocolConfig {

  private final Provider<Iterable<ChannelHandler>> channelsProvider;
  private final Provider<GnutellaSessionModel> sessionModuleProvider;

  public GnutellaProtocolConfig(@Gnutella Provider<Iterable<ChannelHandler>> channelsProvider,
      Provider<GnutellaSessionModel> sessionModuleProvider) {
    this.channelsProvider = channelsProvider;
    this.sessionModuleProvider = sessionModuleProvider;
  }

  @Override
  public SessionModel createSessionModel(Channel channel, Protocol protocol) {
    return sessionModuleProvider.get();
  }

  @Override
  public Iterable<ChannelHandler> createChannelHandlers() {
    return channelsProvider.get();
  }

}

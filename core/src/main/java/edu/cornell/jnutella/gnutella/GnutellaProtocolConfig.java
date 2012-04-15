package edu.cornell.jnutella.gnutella;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;

import com.google.inject.Inject;
import com.google.inject.Provider;

import edu.cornell.jnutella.gnutella.session.GnutellaSessionModel;
import edu.cornell.jnutella.identity.ProtocolIdentityModel;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.ProtocolConfig;
import edu.cornell.jnutella.session.SessionModel;

@Protocol(name = "GNUTELLA", version = "0.6", headerRegex = "^GNUTELLA CONNECT/0\\.6$")
public class GnutellaProtocolConfig implements ProtocolConfig {

  private final Provider<Iterable<ChannelHandler>> channelsProvider;
  private final GnutellaSessionModel.Factory sessionModuleProvider;
  private final Provider<GnutellaIdentityModel> identityModelProvider;

  @Inject
  public GnutellaProtocolConfig(@Gnutella Provider<Iterable<ChannelHandler>> channelsProvider,
      GnutellaSessionModel.Factory sessionModuleProvider,
      Provider<GnutellaIdentityModel> identityModelProvider) {
    this.channelsProvider = channelsProvider;
    this.sessionModuleProvider = sessionModuleProvider;
    this.identityModelProvider = identityModelProvider;
  }

  @Override
  public SessionModel createSessionModel(Channel channel, Protocol protocol) {
    return sessionModuleProvider.createSessionModel(channel, protocol);
  }

  @Override
  public Iterable<ChannelHandler> createChannelHandlers() {
    return channelsProvider.get();
  }

  @Override
  public ProtocolIdentityModel createIdentityModel() {
    return identityModelProvider.get();
  }
}

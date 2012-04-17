package edu.cornell.jnutella.gnutella;

import java.util.Set;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;

import edu.cornell.jnutella.gnutella.session.GnutellaSessionModel;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.identity.ProtocolIdentityModel;
import edu.cornell.jnutella.modules.ProtocolModule;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.ProtocolConfig;
import edu.cornell.jnutella.protocol.session.SessionModel;

@Protocol(name = "GNUTELLA", majorVersion = 0, minorVersion = 6, headerRegex = "^GNUTELLA CONNECT/0\\.6$")
public class GnutellaProtocolConfig implements ProtocolConfig {

  private final Provider<ChannelHandler[]> channelsProvider;
  private final GnutellaSessionModel.Factory sessionModuleProvider;
  private final Provider<GnutellaIdentityModel> identityModelProvider;
  private final Provider<Set<ProtocolModule>> modules;
  private final Protocol protocol;

  @Inject
  public GnutellaProtocolConfig(@Gnutella Provider<ChannelHandler[]> channelsProvider,
      GnutellaSessionModel.Factory sessionModuleProvider,
      Provider<GnutellaIdentityModel> identityModelProvider,
      @Gnutella Provider<Set<ProtocolModule>> modules) {
    this.channelsProvider = channelsProvider;
    this.sessionModuleProvider = sessionModuleProvider;
    this.identityModelProvider = identityModelProvider;
    this.modules = modules;
    this.protocol = this.getClass().getAnnotation(Protocol.class);
  }

  @Override
  public SessionModel createSessionModel(Channel channel, NetworkIdentity identity) {
    return sessionModuleProvider.create(channel, protocol, identity, Sets.newHashSet(modules.get()));
  }

  @Override
  public ChannelHandler[] createProtocolHandlers() {
    return channelsProvider.get();
  }

  @Override
  public ProtocolIdentityModel createIdentityModel() {
    return identityModelProvider.get();
  }

  @Override
  public Protocol get() {
    return protocol;
  }
}

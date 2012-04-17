package edu.cornell.jnutella.network;

import java.net.SocketAddress;
import java.util.List;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.identity.NetworkIdentityManager;
import edu.cornell.jnutella.modules.ProtocolModule;
import edu.cornell.jnutella.network.HttpMessageDecoder.Factory;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.ProtocolConfig;
import edu.cornell.jnutella.protocol.headers.CompatabilityHeaderMerger;
import edu.cornell.jnutella.protocol.headers.Headers;
import edu.cornell.jnutella.protocol.session.ProtocolSessionBootstrapper;
import edu.cornell.jnutella.protocol.session.SessionDownstreamHandshaker;
import edu.cornell.jnutella.protocol.session.SessionModel;
import edu.cornell.jnutella.protocol.session.SessionUpstreamHandshaker;

/**
 * 
 * @author Daniel
 */
public class HandshakeStateBootstrapper {

  @InjectLogger
  private Logger log;
  private final HttpMessageDecoder.Factory decoderFactory;
  private final HttpMessageEncoder.Factory encoderFactory;
  private final SessionUpstreamHandshaker.Factory upShakerProvider;
  private final SessionDownstreamHandshaker.Factory downShakerProvider;
  private final CompatabilityHeaderMerger.Factory mergerFactory;
  private final NetworkIdentityManager identityManager;
  private final ProtocolSessionBootstrapper.Factory protocolBootstrapperFactory;

  @Inject
  public HandshakeStateBootstrapper(Factory decoderFactory,
      HttpMessageEncoder.Factory encoderFactory,
      SessionUpstreamHandshaker.Factory upShakerProvider,
      SessionDownstreamHandshaker.Factory downShakerProvider,
      CompatabilityHeaderMerger.Factory mergerFactory, NetworkIdentityManager identityManager,
      ProtocolSessionBootstrapper.Factory protocolBootstrapperFactory) {
    this.decoderFactory = decoderFactory;
    this.encoderFactory = encoderFactory;
    this.upShakerProvider = upShakerProvider;
    this.downShakerProvider = downShakerProvider;
    this.mergerFactory = mergerFactory;
    this.identityManager = identityManager;
    this.protocolBootstrapperFactory = protocolBootstrapperFactory;
  }

  public ChannelHandler[] createHandshakeHandlers(ProtocolConfig protocolConfig, Channel channel) {
    SocketAddress remoteAddress = channel.getRemoteAddress();

    // grab identity
    NetworkIdentity identity;
    Protocol protocol = protocolConfig.get();
    if (identityManager.hasNetworkIdentity(remoteAddress)) {
      identity = identityManager.getNewtorkIdentity(remoteAddress);
      if (identity.hasCurrentSession(protocol)) {
        log.error("Protocol " + protocol + " already has a session running for identity "
            + identity + ", closing channel.");
        channel.close();
        return null;
      }
    } else {
      log.info("Creating new network identity for address: " + remoteAddress);
      identity = identityManager.createNetworkIdentity();
    }
    identityManager.setNetworkAddress(identity, protocol, remoteAddress);
    // create session
    identity.createNewSession(channel, protocol);

    // register modules
    SessionModel session = identity.getCurrentSession(protocol);
    for (ProtocolModule module : session.getModules()) {
      session.getEventBus().register(module);
    }

    // create header merger
    List<Headers> headersList = Lists.newArrayListWithExpectedSize(session.getModules().size());
    for (ProtocolModule module : session.getModules()) {
      Headers headerAnnotation = module.getClass().getAnnotation(Headers.class);
      if (headerAnnotation == null) {
        log.debug("Module '" + module + "' doesn't have a Headers annotation.");
      } else {
        headersList.add(headerAnnotation);
      }
    }
    Headers[] headers = headersList.toArray(new Headers[headersList.size()]);
    CompatabilityHeaderMerger merger = mergerFactory.create(headers);

    // create handlers
    ChannelHandler[] handlers = new ChannelHandler[4];

    // create protocol bootstrap
    ProtocolSessionBootstrapper protocolBootstrap =
        protocolBootstrapperFactory.create(handlers, protocolConfig.createProtocolHandlers());

    handlers[0] = decoderFactory.create(session);
    handlers[1] = encoderFactory.create(session);
    handlers[2] = upShakerProvider.create(session, merger, protocolBootstrap);
    handlers[3] = downShakerProvider.create(session, protocolBootstrap);
    return handlers;
  }
}

package org.protobee.protocol;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.DefaultChannelFuture;
import org.protobee.guice.SessionScope;
import org.protobee.protocol.headers.CompatabilityHeaderMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;


public class ProtocolGuiceModule extends AbstractModule {

  private final Logger log = LoggerFactory.getLogger(ProtocolGuiceModule.class);

  @Override
  protected void configure() {
    bind(CompatabilityHeaderMerger.class).in(SessionScope.class);
  }

  @Provides
  @HandshakeFuture
  @SessionScope
  public ChannelFuture getHandshakeFuture(Channel channel) {
    log.info("Creating handshake future for channel " + channel);
    return new DefaultChannelFuture(channel, false);
  }

}

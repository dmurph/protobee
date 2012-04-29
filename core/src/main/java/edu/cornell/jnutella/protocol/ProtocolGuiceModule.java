package edu.cornell.jnutella.protocol;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.DefaultChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import edu.cornell.jnutella.guice.SessionScope;
import edu.cornell.jnutella.protocol.headers.CompatabilityHeaderMerger;

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

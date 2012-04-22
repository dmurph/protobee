package edu.cornell.jnutella.protocol;

import org.jboss.netty.channel.ChannelFuture;

import com.google.inject.AbstractModule;

import edu.cornell.jnutella.guice.PrescopedProvider;
import edu.cornell.jnutella.guice.SessionScope;
import edu.cornell.jnutella.protocol.headers.CompatabilityHeaderMerger;

public class ProtocolGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(CompatabilityHeaderMerger.class).in(SessionScope.class);

    bind(ChannelFuture.class).annotatedWith(HandshakeFuture.class)
        .toProvider(new PrescopedProvider<ChannelFuture>(null, "@HandshakeFuture ChannelFuture"))
        .in(SessionScope.class);
  }

}

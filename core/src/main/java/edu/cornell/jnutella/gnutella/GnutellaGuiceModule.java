package edu.cornell.jnutella.gnutella;

import org.jboss.netty.channel.ChannelHandler;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;

import edu.cornell.jnutella.gnutella.messages.decoding.DecodingModule;
import edu.cornell.jnutella.gnutella.messages.decoding.GnutellaDecoderHandler;
import edu.cornell.jnutella.gnutella.messages.encoding.EncodingModule;
import edu.cornell.jnutella.gnutella.session.GnutellaSessionModel;
import edu.cornell.jnutella.guice.GnutellaScopes;
import edu.cornell.jnutella.guice.SessionScoped;
import edu.cornell.jnutella.network.NetworkModule;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.ProtocolConfig;

public class GnutellaGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new NetworkModule());
    install(new DecodingModule());
    install(new EncodingModule());

    bindScope(SessionScoped.class, GnutellaScopes.SESSION);

    install(new FactoryModuleBuilder().build(GnutellaSessionModel.Factory.class));


    Multibinder<ProtocolConfig> protocolBinder =
        Multibinder.newSetBinder(binder(), ProtocolConfig.class);
    protocolBinder.addBinding().to(GnutellaProtocolConfig.class).in(Singleton.class);


    bind(Protocol.class).annotatedWith(Gnutella.class).toProvider(GnutellaProtocolConfig.class)
        .in(Singleton.class);
  }

  @Provides
  @Gnutella
  public ChannelHandler[] getChannelHandlers(GnutellaDecoderHandler decoder) {
    return new ChannelHandler[] {decoder};
  }
}

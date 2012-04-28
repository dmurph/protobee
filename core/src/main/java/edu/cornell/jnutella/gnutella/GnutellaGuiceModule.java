package edu.cornell.jnutella.gnutella;

import org.jboss.netty.channel.ChannelHandler;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import edu.cornell.jnutella.gnutella.constants.GnutellaConstantsModule;
import edu.cornell.jnutella.gnutella.filters.InvalidMessageFilter;
import edu.cornell.jnutella.gnutella.messages.MessageHeader;
import edu.cornell.jnutella.gnutella.messages.decoding.DecodingModule;
import edu.cornell.jnutella.gnutella.messages.decoding.GnutellaDecoderHandler;
import edu.cornell.jnutella.gnutella.messages.encoding.EncodingModule;
import edu.cornell.jnutella.gnutella.messages.encoding.GnutellaEncoderHandler;
import edu.cornell.jnutella.gnutella.modules.ModulesGuiceModule;
import edu.cornell.jnutella.gnutella.session.FlowControlHandler;
import edu.cornell.jnutella.gnutella.session.GnutellaMessageReceiver;
import edu.cornell.jnutella.gnutella.session.GnutellaMessageWriter;
import edu.cornell.jnutella.gnutella.session.GnutellaSessionModel;
import edu.cornell.jnutella.gnutella.session.NoOpFlowControl;
import edu.cornell.jnutella.gnutella.session.PreFilterChannelHandler;
import edu.cornell.jnutella.guice.IdentityScope;
import edu.cornell.jnutella.guice.SessionScope;
import edu.cornell.jnutella.plugin.PluginGuiceModule;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.ProtocolConfig;

public class GnutellaGuiceModule extends PluginGuiceModule {

  @Override
  protected void configure() {
    install(new DecodingModule());
    install(new EncodingModule());
    install(new ModulesGuiceModule());
    install(new GnutellaConstantsModule());

    install(new FactoryModuleBuilder().build(MessageHeader.Factory.class));

    bind(GnutellaSessionModel.class).in(SessionScope.class);
    bind(GnutellaServantModel.class).in(IdentityScope.class);

    addProtocolConfig(GnutellaProtocolConfig.class);

    bind(ProtocolConfig.class).annotatedWith(Gnutella.class).to(GnutellaProtocolConfig.class)
        .in(Singleton.class);

    bind(Protocol.class).annotatedWith(Gnutella.class).toProvider(GnutellaProtocolConfig.class)
        .in(Singleton.class);

    bind(FlowControlHandler.class).to(NoOpFlowControl.class);
    bind(RequestFilter.class).to(SimpleRequestFilter.class).in(Singleton.class);

    bind(SlotsController.class).to(DefaultSlotsController.class).in(Singleton.class);

    addPreFilter(InvalidMessageFilter.class).in(Singleton.class);
  }

  @Provides
  @Gnutella
  public ChannelHandler[] getChannelHandlers(GnutellaDecoderHandler decoder,
      GnutellaEncoderHandler encoder, FlowControlHandler flow, PreFilterChannelHandler prefilter,
      GnutellaMessageReceiver receiver, GnutellaMessageWriter writer) {
    return new ChannelHandler[] {decoder, encoder, flow, writer, prefilter, receiver};
  }
}

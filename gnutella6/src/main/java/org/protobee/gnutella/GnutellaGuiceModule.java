package org.protobee.gnutella;

import org.jboss.netty.channel.ChannelHandler;
import org.protobee.gnutella.constants.GnutellaConstantsModule;
import org.protobee.gnutella.filters.InvalidMessageFilter;
import org.protobee.gnutella.messages.MessageHeader;
import org.protobee.gnutella.messages.decoding.DecodingModule;
import org.protobee.gnutella.messages.decoding.GnutellaDecoderHandler;
import org.protobee.gnutella.messages.encoding.EncodingModule;
import org.protobee.gnutella.messages.encoding.GnutellaEncoderHandler;
import org.protobee.gnutella.modules.ModulesGuiceModule;
import org.protobee.gnutella.session.FlowControlHandler;
import org.protobee.gnutella.session.GnutellaMessageReceiver;
import org.protobee.gnutella.session.GnutellaMessageWriter;
import org.protobee.gnutella.session.GnutellaSessionModel;
import org.protobee.gnutella.session.NoOpFlowControl;
import org.protobee.gnutella.session.PreFilterChannelHandler;
import org.protobee.guice.IdentityScope;
import org.protobee.guice.SessionScope;
import org.protobee.plugin.GnutellaPluginGuiceModule;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolConfig;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;


public class GnutellaGuiceModule extends GnutellaPluginGuiceModule {

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

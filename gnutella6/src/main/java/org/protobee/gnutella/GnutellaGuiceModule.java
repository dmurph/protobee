package org.protobee.gnutella;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.protobee.gnutella.constants.GnutellaConstantsModule;
import org.protobee.gnutella.filters.InvalidMessageFilter;
import org.protobee.gnutella.messages.GnutellaMessage;
import org.protobee.gnutella.messages.MessageHeader;
import org.protobee.gnutella.messages.decoding.DecodingModule;
import org.protobee.gnutella.messages.decoding.GnutellaDecoderHandler;
import org.protobee.gnutella.messages.encoding.EncodingModule;
import org.protobee.gnutella.messages.encoding.GnutellaEncoderHandler;
import org.protobee.gnutella.modules.ModulesGuiceModule;
import org.protobee.gnutella.session.FlowControlHandler;
import org.protobee.gnutella.session.GnutellaPrefilterHandler;
import org.protobee.gnutella.session.MessageReceivedEvent;
import org.protobee.gnutella.session.MessageSendingEvent;
import org.protobee.gnutella.session.NoOpFlowControl;
import org.protobee.guice.scopes.IdentityScope;
import org.protobee.plugin.GnutellaPluginGuiceModule;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolConfig;
import org.protobee.protocol.handlers.ChannelMessagePoster;
import org.protobee.protocol.handlers.ChannelMessagePoster.PosterEventFactory;
import org.protobee.protocol.handlers.DownstreamMessagePosterHandler;
import org.protobee.protocol.handlers.FilterMode;
import org.protobee.protocol.handlers.UpstreamMessagePosterHandler;

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
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
      GnutellaEncoderHandler encoder, FlowControlHandler flow, GnutellaPrefilterHandler prefilter,
      DownstreamMessagePosterHandler.Factory writer, UpstreamMessagePosterHandler.Factory receiver,
      EventBus bus) {

    ChannelMessagePoster sendingPoster =
        new ChannelMessagePoster(
            Sets.<PosterEventFactory<?>>newHashSet(new PosterEventFactory<GnutellaMessage>(
                GnutellaMessage.class) {
              @Override
              public Object createEvent(GnutellaMessage message, ChannelHandlerContext context) {
                return new MessageSendingEvent(context, message);
              }
            }), bus);

    ChannelMessagePoster receivingPoster =
        new ChannelMessagePoster(
            Sets.<PosterEventFactory<?>>newHashSet(new PosterEventFactory<GnutellaMessage>(
                GnutellaMessage.class) {
              @Override
              public Object createEvent(GnutellaMessage message, ChannelHandlerContext context) {
                return new MessageReceivedEvent(context, message);
              }
            }), bus);

    return new ChannelHandler[] {decoder, encoder, flow,
        writer.create(sendingPoster, FilterMode.ERROR_ON_MISMATCHED_TYPE), prefilter,
        receiver.create(receivingPoster, FilterMode.ERROR_ON_MISMATCHED_TYPE)};
  }
}

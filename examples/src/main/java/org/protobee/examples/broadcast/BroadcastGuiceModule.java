package org.protobee.examples.broadcast;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.protobee.events.BasicMessageReceivedEvent;
import org.protobee.events.BasicMessageSendingEvent;
import org.protobee.examples.broadcast.constants.BroadcastConstantsGuiceModule;
import org.protobee.examples.broadcast.filters.InvalidMessageFilter;
import org.protobee.examples.broadcast.modules.BroadcastMessageModule;
import org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage;
import org.protobee.guice.scopes.SessionScope;
import org.protobee.network.handlers.CleanupOnDisconnectHandler;
import org.protobee.network.handlers.CloseOnExceptionHandler;
import org.protobee.plugin.PluginGuiceModule;
import org.protobee.protocol.handlers.ChannelMessagePoster;
import org.protobee.protocol.handlers.ChannelMessagePoster.PosterEventFactory;
import org.protobee.protocol.handlers.DownstreamMessagePosterHandler;
import org.protobee.protocol.handlers.FilterMode;
import org.protobee.protocol.handlers.UpstreamMessagePosterHandler;
import org.protobee.util.PreFilter;

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;

public class BroadcastGuiceModule extends PluginGuiceModule {

  @Override
  protected void configure() {
    install(new BroadcastConstantsGuiceModule());

    addProtocolConfig(BroadcastProtocolConfig.class);

    TypeLiteral<PreFilter<BroadcastMessage>> filterType =
        new TypeLiteral<PreFilter<BroadcastMessage>>() {};
    Multibinder<PreFilter<BroadcastMessage>> filters =
        Multibinder.newSetBinder(binder(), filterType, Broadcast.class);

    filters.addBinding().to(InvalidMessageFilter.class).in(Singleton.class);


    addModuleBinding(BroadcastMessageModule.class, Broadcast.class).in(SessionScope.class);
  }


  @Provides
  @Broadcast
  public ChannelHandler[] getProtocolHandlers(EventBus eventBus, ProtobufEncoder encoder,
      DownstreamMessagePosterHandler.Factory writerFactory,
      UpstreamMessagePosterHandler.Factory readerFactory, CleanupOnDisconnectHandler cleanup,
      CloseOnExceptionHandler closeOnException, LoggingHandler logging) {
    ChannelMessagePoster writerPoster =
        new ChannelMessagePoster(
            Sets.<PosterEventFactory<?>>newHashSet(new PosterEventFactory<BroadcastMessage>(
                BroadcastMessage.class) {
              @Override
              public Object createEvent(BroadcastMessage message, ChannelHandlerContext context) {
                return new BasicMessageSendingEvent(context, message);
              }
            }), eventBus);

    ChannelMessagePoster readerPoster =
        new ChannelMessagePoster(
            Sets.<PosterEventFactory<?>>newHashSet(new PosterEventFactory<BroadcastMessage>(
                BroadcastMessage.class) {
              @Override
              public Object createEvent(BroadcastMessage message, ChannelHandlerContext context) {
                return new BasicMessageReceivedEvent(context, message);
              }
            }), eventBus);

    return new ChannelHandler[] {encoder,
        new ProtobufDecoder(BroadcastMessage.getDefaultInstance()),
        writerFactory.create(writerPoster, FilterMode.ERROR_ON_MISMATCHED_TYPE),
        readerFactory.create(readerPoster, FilterMode.ERROR_ON_MISMATCHED_TYPE), logging,
        closeOnException, cleanup};
  }

}
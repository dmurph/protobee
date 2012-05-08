package org.protobee.examples.emotion;

import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ServerChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.logging.InternalLogLevel;
import org.protobee.events.BasicMessageReceivedEvent;
import org.protobee.events.BasicMessageSendingEvent;
import org.protobee.examples.emotion.constants.EmotionConstantsGuiceModule;
import org.protobee.examples.emotion.filters.InvalidMessageFilter;
import org.protobee.examples.emotion.modules.EmotionMessageModule;
import org.protobee.examples.protos.EmotionProtos.EmotionMessage;
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

public class EmotionGuiceModule extends PluginGuiceModule {

  @Override
  protected void configure() {
    install(new EmotionConstantsGuiceModule());

    addProtocolConfig(EmotionProtocolConfig.class, Emotion.class);

    TypeLiteral<PreFilter<EmotionMessage>> filterType =
        new TypeLiteral<PreFilter<EmotionMessage>>() {};
    Multibinder<PreFilter<EmotionMessage>> filters =
        Multibinder.newSetBinder(binder(), filterType, Emotion.class);

    filters.addBinding().to(InvalidMessageFilter.class).in(Singleton.class);

    bind(ChannelFactory.class).annotatedWith(Emotion.class).to(NioClientSocketChannelFactory.class)
        .in(Singleton.class);
    bind(ServerChannelFactory.class).annotatedWith(Emotion.class)
        .to(NioServerSocketChannelFactory.class).in(Singleton.class);

    addModuleBinding(EmotionMessageModule.class, Emotion.class).in(SessionScope.class);

    bind(EmotionProvider.class);
  }


  @Provides
  @Emotion
  public ChannelHandler[] getProtocolHandlers(EventBus eventBus, ProtobufEncoder encoder,
      DownstreamMessagePosterHandler.Factory writerFactory,
      UpstreamMessagePosterHandler.Factory readerFactory, CleanupOnDisconnectHandler cleanup,
      CloseOnExceptionHandler closeOnException) {
    ChannelMessagePoster writerPoster =
        new ChannelMessagePoster(
            Sets.<PosterEventFactory<?>>newHashSet(new PosterEventFactory<EmotionMessage>(
                EmotionMessage.class) {
              @Override
              public Object createEvent(EmotionMessage message, ChannelHandlerContext context) {
                return new BasicMessageSendingEvent(context, message);
              }
            }), eventBus);

    ChannelMessagePoster readerPoster =
        new ChannelMessagePoster(
            Sets.<PosterEventFactory<?>>newHashSet(new PosterEventFactory<EmotionMessage>(
                EmotionMessage.class) {
              @Override
              public Object createEvent(EmotionMessage message, ChannelHandlerContext context) {
                return new BasicMessageReceivedEvent(context, message);
              }
            }), eventBus);

    return new ChannelHandler[] {
        encoder,
        new ProtobufDecoder(EmotionMessage.getDefaultInstance()),
        new LoggingHandler("org.protobee.examples.emotions.EmotionLoggingHandler",
            InternalLogLevel.DEBUG, false),
        writerFactory.create(writerPoster, FilterMode.ERROR_ON_MISMATCHED_TYPE),
        readerFactory.create(readerPoster, FilterMode.ERROR_ON_MISMATCHED_TYPE), closeOnException,
        cleanup};
  }

}

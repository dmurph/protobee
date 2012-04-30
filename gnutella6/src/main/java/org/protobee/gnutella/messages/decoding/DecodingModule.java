package org.protobee.gnutella.messages.decoding;

import org.protobee.gnutella.Gnutella;
import org.protobee.gnutella.messages.MessageBodyFactory;
import org.protobee.gnutella.messages.MessageHeader;
import org.protobee.gnutella.session.ForMessageTypes;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;


public class DecodingModule extends AbstractModule {

  @SuppressWarnings("rawtypes")
  @Override
  protected void configure() {
    // bind body factory
    install(new FactoryModuleBuilder().build(MessageBodyFactory.class));

    // bind header decoder
    bind(MessageHeaderDecoder.class).to(MessageHeaderDecoderImpl.class).in(Singleton.class);

    // bind body decoders
    bind(MessageBodyDecoder.class).annotatedWith(ForMessageTypes.with(MessageHeader.F_PING)).to(
      PingDecoder.class);
    bind(MessageBodyDecoder.class).annotatedWith(ForMessageTypes.with(MessageHeader.F_PING_REPLY)).to(
      PongDecoder.class);
    bind(MessageBodyDecoder.class).annotatedWith(ForMessageTypes.with(MessageHeader.F_QUERY)).to(
      QueryDecoder.class);
    bind(MessageBodyDecoder.class).annotatedWith(ForMessageTypes.with(MessageHeader.F_QUERY_REPLY)).to(
      QueryHitDecoder.class);
    bind(MessageBodyDecoder.class).annotatedWith(ForMessageTypes.with(MessageHeader.F_PUSH)).to(
      PushDecoder.class);

    // add body decoders to set {};
    Multibinder<MessageBodyDecoder> gnutellaDecoders =
        Multibinder.newSetBinder(binder(), MessageBodyDecoder.class, Gnutella.class);

    gnutellaDecoders.addBinding().to( 
      Key.get(MessageBodyDecoder.class, ForMessageTypes.with(MessageHeader.F_PING)));
    Key.get(MessageBodyDecoder.class, ForMessageTypes.with(MessageHeader.F_PING_REPLY));
    Key.get(MessageBodyDecoder.class, ForMessageTypes.with(MessageHeader.F_QUERY));
    Key.get(MessageBodyDecoder.class, ForMessageTypes.with(MessageHeader.F_QUERY_REPLY));
    Key.get(MessageBodyDecoder.class, ForMessageTypes.with(MessageHeader.F_PUSH));
  }
}

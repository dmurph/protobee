package edu.cornell.jnutella.gnutella.messages.decoding;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;

import edu.cornell.jnutella.gnutella.Gnutella;
import edu.cornell.jnutella.gnutella.messages.MessageBodyFactory;
import edu.cornell.jnutella.gnutella.messages.MessageHeader;
import edu.cornell.jnutella.gnutella.session.ForMessageTypes;

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

    // add body decoders to set {};
    Multibinder<MessageBodyDecoder> gnutellaDecoders =
        Multibinder.newSetBinder(binder(), MessageBodyDecoder.class, Gnutella.class);

    gnutellaDecoders.addBinding().to( 
      Key.get(MessageBodyDecoder.class, ForMessageTypes.with(MessageHeader.F_PING)));
    Key.get(MessageBodyDecoder.class, ForMessageTypes.with(MessageHeader.F_PING_REPLY));
    Key.get(MessageBodyDecoder.class, ForMessageTypes.with(MessageHeader.F_QUERY));
    Key.get(MessageBodyDecoder.class, ForMessageTypes.with(MessageHeader.F_PUSH));
  }
}

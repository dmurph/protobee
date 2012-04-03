package edu.cornell.jnutella.messages.decoding;

import javax.xml.soap.MessageFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;

import edu.cornell.jnutella.annotation.Gnutella;
import edu.cornell.jnutella.messages.MessageHeader;
import edu.cornell.jnutella.session.gnutella.ForMessageTypes;

public class EncodingModule extends AbstractModule {

  @Override
  protected void configure() {
    // bind body factory
    install(new FactoryModuleBuilder().build(MessageFactory.class));

    // bind header decoder
    bind(MessageHeaderDecoder.class).to(MessageHeaderDecoderImpl.class).in(Singleton.class);

    // bind body decoders
    bind(MessageBodyDecoder.class).annotatedWith(ForMessageTypes.with(MessageHeader.F_PING)).to(
        PingDecoder.class);

    // add body decoders to set
    Multibinder<MessageBodyDecoder> gnutellaDecoders =
        Multibinder.newSetBinder(binder(), MessageBodyDecoder.class, Gnutella.class);

    gnutellaDecoders.addBinding().to(
        Key.get(MessageBodyDecoder.class, ForMessageTypes.with(MessageHeader.F_PING)));
  }
}

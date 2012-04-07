package edu.cornell.jnutella.messages.encoding;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

import edu.cornell.jnutella.annotation.Gnutella;

public class EncodingModule extends AbstractModule {

  @Override
  protected void configure() {

    // bind header decoder
    bind(MessageHeaderEncoder.class).to(MessageHeaderEncoderImpl.class).in(Singleton.class);

    // bind body decoders

    // add body decoders to set {};
    Multibinder<MessageBodyEncoder> gnutellaDecoders =
        Multibinder.newSetBinder(binder(), MessageBodyEncoder.class, Gnutella.class);

//    gnutellaDecoders.addBinding().to( 
//        Key.get(MessageBodyEncoder.class, ForMessageTypes.with(MessageHeader.F_PING)));
  }
}

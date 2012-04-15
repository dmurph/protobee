package edu.cornell.jnutella.messages.encoding;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

import edu.cornell.jnutella.gnutella.Gnutella;
import edu.cornell.jnutella.gnutella.session.ForMessageTypes;
import edu.cornell.jnutella.messages.MessageHeader;

public class EncodingModule extends AbstractModule {

  @Override
  protected void configure() {

    // bind header encoder
    bind(MessageHeaderEncoder.class).to(MessageHeaderEncoderImpl.class).in(Singleton.class);

    // bind body encoder
    bind(MessageBodyEncoder.class).annotatedWith(ForMessageTypes.with(MessageHeader.F_PING)).to(
      PingEncoder.class);
    bind(MessageBodyEncoder.class).annotatedWith(ForMessageTypes.with(MessageHeader.F_PING_REPLY)).to(
      PongEncoder.class);
    bind(MessageBodyEncoder.class).annotatedWith(ForMessageTypes.with(MessageHeader.F_QUERY)).to(
      QueryEncoder.class);
    bind(MessageBodyEncoder.class).annotatedWith(ForMessageTypes.with(MessageHeader.F_PUSH)).to(
      QueryEncoder.class);

    // add body encoder to set {};
    Multibinder<MessageBodyEncoder> gnutellaEncoders =
        Multibinder.newSetBinder(binder(), MessageBodyEncoder.class, Gnutella.class);

    gnutellaEncoders.addBinding().to( 
      Key.get(MessageBodyEncoder.class, ForMessageTypes.with(MessageHeader.F_PING)));
      Key.get(MessageBodyEncoder.class, ForMessageTypes.with(MessageHeader.F_PING_REPLY));
      Key.get(MessageBodyEncoder.class, ForMessageTypes.with(MessageHeader.F_QUERY));
      Key.get(MessageBodyEncoder.class, ForMessageTypes.with(MessageHeader.F_PUSH));

  }



}

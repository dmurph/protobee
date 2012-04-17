package edu.cornell.jnutella.protocol;

import com.google.inject.AbstractModule;

import edu.cornell.jnutella.protocol.session.HandshakeInterruptor;
import edu.cornell.jnutella.protocol.session.HandshakeInterruptorImpl;

public class ProtocolGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(HandshakeInterruptor.class).to(HandshakeInterruptorImpl.class);
  }

}

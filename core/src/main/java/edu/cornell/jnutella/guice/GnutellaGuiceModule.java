package edu.cornell.jnutella.guice;

import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;

import edu.cornell.jnutella.messages.decoding.DecodingModule;
import edu.cornell.jnutella.messages.encoding.EncodingModule;
import edu.cornell.jnutella.network.NetworkModule;
import edu.cornell.jnutella.protocol.CompatibilityHeader;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.ProtocolHeader;
import edu.cornell.jnutella.protocol.VenderHeader;

public class GnutellaGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
    install(new NetworkModule());
    install(new DecodingModule());
    install(new EncodingModule());

    bindListener(Matchers.any(), new Slf4jTypeListener());


    bindScope(SessionScoped.class, GnutellaScopes.SESSION);



    Multibinder<Protocol> protocolBinder = Multibinder.newSetBinder(binder(), Protocol.class);
    // bind protocols here

    Multibinder<ProtocolHeader> compatabilityHeaders =
        Multibinder.newSetBinder(binder(), ProtocolHeader.class, CompatibilityHeader.class);
    // bind compatibility headers here

    Multibinder<ProtocolHeader> venderHeaders =
        Multibinder.newSetBinder(binder(), ProtocolHeader.class, VenderHeader.class);
    // bind vender headers here
  }

}

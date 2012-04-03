package edu.cornell.jnutella.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.util.Modules;

import edu.cornell.jnutella.guice.netty.NettyModule;
import edu.cornell.jnutella.messages.decoding.DecodingModule;
import edu.cornell.jnutella.network.NetworkModule;
import edu.cornell.jnutella.protocol.BasicProtocolHeader;
import edu.cornell.jnutella.protocol.CompatibilityHeader;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.ProtocolHeader;
import edu.cornell.jnutella.protocol.VenderHeader;

public class GnutellaGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new NetworkModule());
    install(new DecodingModule());

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

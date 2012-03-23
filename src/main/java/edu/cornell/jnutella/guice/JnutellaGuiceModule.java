package edu.cornell.jnutella.guice;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import edu.cornell.jnutella.protocol.CompatibilityHeader;
import edu.cornell.jnutella.protocol.GnutellaProtocol;
import edu.cornell.jnutella.protocol.ProtocolHeader;
import edu.cornell.jnutella.protocol.VenderHeader;

public class JnutellaGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    bindScope(SessionScoped.class, JnutellaScopes.SESSION);
    Multibinder<GnutellaProtocol> protocolBinder = Multibinder.newSetBinder(binder(), GnutellaProtocol.class);
    // bind protocols here
    
    Multibinder<ProtocolHeader> compatabilityHeaders = Multibinder.newSetBinder(binder(), ProtocolHeader.class, CompatibilityHeader.class);
    // bind compatibility headers here
    
    Multibinder<ProtocolHeader> venderHeaders = Multibinder.newSetBinder(binder(), ProtocolHeader.class, VenderHeader.class);
    // bind vender headers here
  }

}

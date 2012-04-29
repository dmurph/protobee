package edu.cornell.jnutella.gnutella.modules;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import edu.cornell.jnutella.gnutella.session.MessageReceivedEvent;
import edu.cornell.jnutella.guice.SessionScope;
import edu.cornell.jnutella.modules.ProtocolModule;

@SessionScope
public class PingModule<ProtocolMessageWriter> implements ProtocolModule {

  @Inject
  public PingModule() {
  }

  @Subscribe
  public void messageReceived(MessageReceivedEvent event) {
    
  }


}
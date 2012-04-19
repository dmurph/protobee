package edu.cornell.jnutella.gnutella.modules;

import com.google.common.eventbus.Subscribe;

import edu.cornell.jnutella.gnutella.messages.GnutellaMessage;
import edu.cornell.jnutella.gnutella.session.MessageReceivedEvent;
import edu.cornell.jnutella.modules.ProtocolModule;

public class PingModule implements ProtocolModule {

  @Subscribe
  public void messageReceived(MessageReceivedEvent message) {
  }
}

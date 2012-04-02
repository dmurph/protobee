package edu.cornell.jnutella.modules.listeners;

import edu.cornell.jnutella.messages.GnutellaMessage;

public interface GnutellaMessageListener {

  void sending(GnutellaMessage message);

  void sent(GnutellaMessage message);

  void receiving(GnutellaMessage message);

  void received(GnutellaMessage message);
}

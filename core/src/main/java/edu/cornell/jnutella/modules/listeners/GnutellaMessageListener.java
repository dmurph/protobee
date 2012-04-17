package edu.cornell.jnutella.modules.listeners;

import edu.cornell.jnutella.gnutella.messages.MessageHeader;

public interface GnutellaMessageListener {

  void sending(MessageHeader message);

  void sent(MessageHeader message);

  void receiving(MessageHeader message);

  void received(MessageHeader message);
}

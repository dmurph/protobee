package edu.cornell.jnutella.messages;

import edu.cornell.jnutella.extension.GGEP;

public interface MessageBodyFactory {
  PingMessage createPingMessage(GGEP ggep);
}

package edu.cornell.jnutella.gnutella.modules;

import edu.cornell.jnutella.gnutella.messages.GnutellaMessage;
import edu.cornell.jnutella.gnutella.messages.PongBody;

public interface PongCache {

  void addPong(GnutellaMessage message);
  
  int size();

  void filter(long maxTimeMillis);

  Iterable<PongBody> getPongs(int num);
}

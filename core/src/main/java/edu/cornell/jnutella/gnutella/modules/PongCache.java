package edu.cornell.jnutella.gnutella.modules;

import java.net.SocketAddress;

import edu.cornell.jnutella.gnutella.messages.GnutellaMessage;
import edu.cornell.jnutella.gnutella.messages.PongBody;

public interface PongCache {

  void addPong(GnutellaMessage message);
  
  int size();

  Iterable<PongBody> getPongs(SocketAddress destAddress, int num);

  void filter(long maxTimeMillis);
}

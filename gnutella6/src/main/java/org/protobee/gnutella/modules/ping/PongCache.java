package org.protobee.gnutella.modules.ping;

import org.protobee.gnutella.messages.GnutellaMessage;
import org.protobee.gnutella.messages.PongBody;

public interface PongCache {

  void addPong(GnutellaMessage message);
  
  int size();

  void filter(long maxTimeMillis);

  Iterable<PongBody> getPongs(int num);
}

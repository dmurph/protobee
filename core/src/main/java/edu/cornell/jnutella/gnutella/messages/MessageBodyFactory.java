package edu.cornell.jnutella.gnutella.messages;

import java.net.InetAddress;

import com.google.inject.assistedinject.Assisted;

import edu.cornell.jnutella.extension.GGEP;
import edu.cornell.jnutella.util.GUID;

public interface MessageBodyFactory {
  PingBody createPingMessage(@Assisted("ggep") GGEP ggep);

  PongBody createPongMessage(InetAddress address, int port,
                             @Assisted("fileCount") long fileCount, @Assisted("fileSizeInKB") long fileSizeInKB, @Assisted("ggep") GGEP ggep);

  QueryBody createQueryMessage(short minSpeed, String query, GGEP ggep);
  
  PushBody createPushMessage(GUID servantID, long index, 
                             InetAddress address, int port, GGEP ggep);
  
  
}

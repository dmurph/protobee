package edu.cornell.jnutella.messages;

import java.net.InetAddress;

import com.google.inject.assistedinject.Assisted;

import edu.cornell.jnutella.extension.GGEP;

public interface MessageBodyFactory {
  PingBody createPingMessage(@Assisted("ggep") GGEP ggep);

  PongBody createPongMessage(InetAddress address, int port,
                             @Assisted("fileCount") long fileCount, @Assisted("fileSizeInKB") long fileSizeInKB, @Assisted("ggep") GGEP ggep);

  QueryBody createQueryMessage(short minSpeed, String query, GGEP ggep);
}

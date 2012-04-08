package edu.cornell.jnutella.messages;

import java.net.InetAddress;

import com.google.inject.name.Named;

import edu.cornell.jnutella.extension.GGEP;

public interface MessageBodyFactory {
  PingBody createPingMessage(GGEP ggep);

  PongBody createPongMessage(InetAddress address, @Named("port") int port,
      @Named("fileCount") long fileCount, @Named("fileSizeInKB") long fileSizeInKB, GGEP ggep);

  QueryBody createQueryMessage(short minSpeed, String query, GGEP ggep);
}

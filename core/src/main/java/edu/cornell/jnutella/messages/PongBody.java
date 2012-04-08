package edu.cornell.jnutella.messages;

import java.net.InetAddress;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.name.Named;

import edu.cornell.jnutella.extension.GGEP;

public class PongBody implements MessageBody {

  private final InetAddress address;
  private final int port;
  private final long fileCount;
  private final long fileSizeInKB;
  private final GGEP ggep;

  @AssistedInject
  public PongBody(@Assisted InetAddress address, @Assisted @Named("port") int port,
      @Assisted @Named("fileCount") long fileCount,
      @Assisted @Named("fileSizeInKB") long fileSizeInKB, @Assisted GGEP ggep) {
    this.port = port;
    this.address = address;
    this.fileCount = fileCount;
    this.fileSizeInKB = fileSizeInKB;
    this.ggep = ggep;
  }

  public InetAddress getAddress() {
    return address;
  }

  public int getPort() {
    return port;
  }

  public long getFileCount() {
    return fileCount;
  }

  public long getFileSizeInKB() {
    return fileSizeInKB;
  }

  public GGEP getGgep() {
    return ggep;
  }
}

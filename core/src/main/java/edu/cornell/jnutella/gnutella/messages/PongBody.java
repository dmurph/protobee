package edu.cornell.jnutella.gnutella.messages;

import java.net.InetAddress;

import javax.annotation.Nullable;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import edu.cornell.jnutella.extension.GGEP;

public class PongBody implements MessageBody {

  private final InetAddress address;
  private final int port;
  private final long fileCount;
  private final long fileSizeInKB;
  private final GGEP ggep;

  @AssistedInject
  public PongBody(@Assisted InetAddress address, @Assisted int port,
      @Assisted("fileCount") long fileCount,
      @Assisted("fileSizeInKB") long fileSizeInKB, @Nullable @Assisted("ggep") GGEP ggep) {
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((address == null) ? 0 : address.hashCode());
    result = prime * result + (int) (fileCount ^ (fileCount >>> 32));
    result = prime * result + (int) (fileSizeInKB ^ (fileSizeInKB >>> 32));
    result = prime * result + ((ggep == null) ? 0 : ggep.hashCode());
    result = prime * result + port;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    PongBody other = (PongBody) obj;
    if (address == null) {
      if (other.address != null) return false;
    } else if (!address.equals(other.address)) return false;
    if (fileCount != other.fileCount) return false;
    if (fileSizeInKB != other.fileSizeInKB) return false;
    if (ggep == null) {
      if (other.ggep != null) return false;
    } else if (!ggep.equals(other.ggep)) return false;
    if (port != other.port) return false;
    return true;
  }
}

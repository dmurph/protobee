package edu.cornell.jnutella.gnutella.messages;

import java.net.SocketAddress;

import javax.annotation.Nullable;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import edu.cornell.jnutella.extension.GGEP;

public class PongBody implements MessageBody {

  private final SocketAddress socketAddress;
  private final long fileCount;
  private final long fileSizeInKB;
  private final GGEP ggep;

  @AssistedInject
  public PongBody(@Assisted SocketAddress address, @Assisted("fileCount") long fileCount,
      @Assisted("fileSizeInKB") long fileSizeInKB, @Nullable @Assisted("ggep") GGEP ggep) {

    this.socketAddress = address;
    this.fileCount = fileCount;
    this.fileSizeInKB = fileSizeInKB;
    this.ggep = ggep;
  }

  public SocketAddress getSocketAddress() {
    return socketAddress;
  }

  /**
   * Returns true if this pong is marking a ultrapeer. This is the case when fileSizeInKB is a power
   * of two but at least 8.
   * 
   * @return true if this pong is marking a ultrapeer.
   */
  public boolean isUltrapeerMarked() {
    if (fileSizeInKB < 8) {
      return false;
    }
    return (fileSizeInKB & (fileSizeInKB - 1)) == 0;
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
    result = prime * result + (int) (fileCount ^ (fileCount >>> 32));
    result = prime * result + (int) (fileSizeInKB ^ (fileSizeInKB >>> 32));
    result = prime * result + ((ggep == null) ? 0 : ggep.hashCode());
    result = prime * result + ((socketAddress == null) ? 0 : socketAddress.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    PongBody other = (PongBody) obj;
    if (fileCount != other.fileCount) return false;
    if (fileSizeInKB != other.fileSizeInKB) return false;
    if (ggep == null) {
      if (other.ggep != null) return false;
    } else if (!ggep.equals(other.ggep)) return false;
    if (socketAddress == null) {
      if (other.socketAddress != null) return false;
    } else if (!socketAddress.equals(other.socketAddress)) return false;
    return true;
  }

}

package org.protobee.gnutella.messages;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import javax.annotation.Nullable;

import org.protobee.gnutella.extension.GGEP;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;


public class PongBody implements MessageBody {

  private final InetSocketAddress socketAddress;
  private final long fileCount;
  private final long fileSizeInKB;
  private final GGEP ggep;

  @AssistedInject
  public PongBody(@Assisted InetSocketAddress address,
      @Assisted("fileCount") long fileCount,
      @Assisted("fileSizeInKB") long fileSizeInKB, 
      @Nullable @Assisted("ggep") GGEP ggep) {
    
    this.socketAddress = address;
    this.fileCount = fileCount;
    this.fileSizeInKB = fileSizeInKB;
    this.ggep = (ggep == null || ggep.isEmpty()) ? null : ggep;
  }

  public InetSocketAddress getSocketAddress(){
    return socketAddress;
  }
  
  public InetAddress getAddress() {
    return socketAddress.getAddress();
  }

  public int getPort() {
    return socketAddress.getPort();
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
  public String toString() {
    return "{ address: " + socketAddress + ", fileCount: " + fileCount + ", fileSizeInKB: "
        + fileSizeInKB + ", ggep: " + ggep + "}";
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

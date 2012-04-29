package org.protobee.gnutella.messages;

import org.protobee.extension.GGEP;
import org.protobee.util.URN;

public class ResponseBody {

  private long fileIndex;
  private long fileSize;
  private String fileName;
  private URN urn;
  private GGEP ggep;
  
  public ResponseBody(long fileIndex, long fileSize, String fileName, URN urn, GGEP ggep) {
    this.fileIndex = fileIndex;
    this.fileSize = fileSize;
    this.fileName = fileName;
    this.urn = urn;
    this.ggep = ggep;
  }


  public long getFileIndex() {
    return fileIndex;
  }


  public long getFileSize() {
    return fileSize;
  }


  public String getFileName() {
    return fileName;
  }


  public GGEP getGgep() {
    return ggep;
  }


  public URN getURN() {
    return urn;
  }


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (fileIndex ^ (fileIndex >>> 32));
    result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
    result = prime * result + (int) (fileSize ^ (fileSize >>> 32));
    result = prime * result + ((ggep == null) ? 0 : ggep.hashCode());
    result = prime * result + ((urn == null) ? 0 : urn.hashCode());
    return result;
  }


  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ResponseBody other = (ResponseBody) obj;
    if (fileIndex != other.fileIndex) return false;
    if (fileName == null) {
      if (other.fileName != null) return false;
    } else if (!fileName.equals(other.fileName)) return false;
    if (fileSize != other.fileSize) return false;
    if (ggep == null) {
      if (other.ggep != null) return false;
    } else if (!ggep.equals(other.ggep)) return false;
    if (urn == null) {
      if (other.urn != null) return false;
    } else if (!urn.equals(other.urn)) return false;
    return true;
  }
  
}

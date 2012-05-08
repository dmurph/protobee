package org.protobee.gnutella.messages;

import org.protobee.gnutella.extension.GGEP;
import org.protobee.gnutella.extension.HUGEExtension;
import org.protobee.gnutella.file.ShareFile;

public class ResponseBody {

  private long fileIndex;
  private long fileSize;
  private String fileName;
  private HUGEExtension huge;
  private GGEP ggep;
  
  public ResponseBody(long fileIndex, long fileSize, String fileName, HUGEExtension huge, GGEP ggep) {
    this.fileIndex = fileIndex;
    this.fileSize = fileSize;
    this.fileName = fileName;
    this.huge = (huge == null || huge.isEmpty()) ? null : huge;
    this.ggep = (ggep == null || ggep.isEmpty()) ? null : ggep;
  }

  public long getFileIndex() {
    return fileIndex;
  }

  public static ResponseBody noOpCreateFromShareFile(ShareFile file){
    return new ResponseBody(Long.MAX_VALUE, Long.MAX_VALUE, "NOOP", null, null);
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


  public HUGEExtension getHUGE() {
    return huge;
  }


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (fileIndex ^ (fileIndex >>> 32));
    result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
    result = prime * result + (int) (fileSize ^ (fileSize >>> 32));
    result = prime * result + ((ggep == null) ? 0 : ggep.hashCode());
    result = prime * result + ((huge == null) ? 0 : huge.hashCode());
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
    if (huge == null) {
      if (other.huge != null) return false;
    } else if (!huge.equals(other.huge)) return false;
    return true;
  }
  
}

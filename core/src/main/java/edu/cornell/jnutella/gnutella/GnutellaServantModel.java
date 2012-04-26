package edu.cornell.jnutella.gnutella;

import edu.cornell.jnutella.guice.IdentityScope;

@IdentityScope
public class GnutellaServantModel {

  private byte[] guid;
  private int fileCount = 0;
  private int fileSizeInKB = 0;


  public byte[] getGuid() {
    return guid;
  }

  public void setGuid(byte[] guid) {
    this.guid = guid;
  }

  public int getFileCount() {
    return fileCount;
  }

  public void setFileCount(int fileCount) {
    this.fileCount = fileCount;
  }

  public int getFileSizeInKB() {
    return fileSizeInKB;
  }

  public void setFileSizeInKB(int fileSizeInKB) {
    this.fileSizeInKB = fileSizeInKB;
  }
}

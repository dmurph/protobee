package org.protobee.gnutella.file;

import org.protobee.gnutella.util.URN;

/**
 * Represents a file for gnutella to share
 * 
 * @author Daniel
 */
public interface ShareFile {

  URN getURN();

  String getSha1();

  int getFileIndex();

  long getFileSize();
}

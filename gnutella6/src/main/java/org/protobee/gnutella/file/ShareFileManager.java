package org.protobee.gnutella.file;

import java.util.List;

import org.protobee.gnutella.util.URN;

/**
 * Manager for all shared files
 * 
 * @author Daniel
 */
public interface ShareFileManager {
  List<ShareFile> getFiles();

  List<ShareFile> getFiles(URN[] urns);

  ShareFile getFile(URN urn);

  ShareFile getFile(int indexNumber);

  ShareFile getFile(String name);
  
  int getFileCount();
}

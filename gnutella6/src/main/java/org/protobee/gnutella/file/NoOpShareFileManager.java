package org.protobee.gnutella.file;

import java.util.List;

import org.protobee.gnutella.util.URN;

import com.google.common.collect.ImmutableList;
import com.google.inject.Singleton;

@Singleton
public class NoOpShareFileManager implements ShareFileManager {

  @Override
  public List<ShareFile> getFiles() {
    return ImmutableList.of();
  }

  @Override
  public List<ShareFile> getFiles(URN[] urns) {
    return ImmutableList.of();
  }

  @Override
  public ShareFile getFile(URN urn) {
    return null;
  }

  @Override
  public ShareFile getFile(int indexNumber) {
    return null;
  }

  @Override
  public ShareFile getFile(String name) {
    return null;
  }

  @Override
  public int getFileCount() {
    return 0;
  }
}

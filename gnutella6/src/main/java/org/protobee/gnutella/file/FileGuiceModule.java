package org.protobee.gnutella.file;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class FileGuiceModule extends AbstractModule {

  protected void configure() {
    bind(ShareFileManager.class).to(NoOpShareFileManager.class).in(Singleton.class);
  }
}

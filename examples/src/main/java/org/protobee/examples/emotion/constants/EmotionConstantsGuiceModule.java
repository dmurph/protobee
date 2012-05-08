package org.protobee.examples.emotion.constants;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import com.google.inject.AbstractModule;

public class EmotionConstantsGuiceModule extends AbstractModule {
  @Override
  protected void configure() { 
    
    bindConstant().annotatedWith(MaxHops.class).to(6);
    bindConstant().annotatedWith(MaxTtl.class).to(6);
    bindConstant().annotatedWith(IdSize.class).to(16);
    
    bind(SocketAddress.class).annotatedWith(EmotionListeningAddress.class).toInstance(
      new InetSocketAddress(32411));
  }

}


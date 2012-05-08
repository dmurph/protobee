package org.protobee.examples.emotion.constants;


import java.net.SocketAddress;

import org.jboss.netty.channel.local.LocalAddress;

import com.google.inject.AbstractModule;

public class EmotionReplyConstantsGuiceModule extends AbstractModule {
  @Override
  protected void configure() { 
    bind(SocketAddress.class).annotatedWith(EmotionReplyListeningAddress.class).toInstance(
      new LocalAddress("broadcast-example"));
  }

}


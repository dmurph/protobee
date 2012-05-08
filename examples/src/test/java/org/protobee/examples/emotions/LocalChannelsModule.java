package org.protobee.examples.emotions;

import java.net.SocketAddress;

import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ServerChannelFactory;
import org.jboss.netty.channel.local.DefaultLocalClientChannelFactory;
import org.jboss.netty.channel.local.DefaultLocalServerChannelFactory;
import org.jboss.netty.channel.local.LocalAddress;
import org.protobee.examples.emotion.Emotion;
import org.protobee.examples.emotion.constants.EmotionListeningAddress;

import com.google.inject.AbstractModule;

public class LocalChannelsModule extends AbstractModule {

  public static final String LOCAL_ADDRESS = "127.0.0.1:453";
  @Override
  protected void configure() {
    bind(ChannelFactory.class).annotatedWith(Emotion.class).to(
      DefaultLocalClientChannelFactory.class);
    bind(ServerChannelFactory.class).annotatedWith(Emotion.class).to(
      DefaultLocalServerChannelFactory.class);

    bind(SocketAddress.class).annotatedWith(EmotionListeningAddress.class).toInstance(
      new LocalAddress(LOCAL_ADDRESS));
  }
}

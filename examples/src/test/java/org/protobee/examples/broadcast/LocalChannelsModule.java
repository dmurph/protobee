package org.protobee.examples.broadcast;

import java.net.SocketAddress;

import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ServerChannelFactory;
import org.jboss.netty.channel.local.DefaultLocalClientChannelFactory;
import org.jboss.netty.channel.local.DefaultLocalServerChannelFactory;
import org.jboss.netty.channel.local.LocalAddress;
import org.protobee.examples.broadcast.constants.BroadcastListeningAddress;

import com.google.inject.AbstractModule;

public class LocalChannelsModule extends AbstractModule {

  public static final String LOCAL_ADDRESS = "127.0.0.1:453";
  @Override
  protected void configure() {
    bind(ChannelFactory.class).annotatedWith(Broadcast.class).to(
        DefaultLocalClientChannelFactory.class);
    bind(ServerChannelFactory.class).annotatedWith(Broadcast.class).to(
        DefaultLocalServerChannelFactory.class);

    bind(SocketAddress.class).annotatedWith(BroadcastListeningAddress.class).toInstance(
        new LocalAddress(LOCAL_ADDRESS));
  }
}

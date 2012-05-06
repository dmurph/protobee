package org.protobee.examples.broadcast.constants;

import java.net.SocketAddress;

import org.jboss.netty.channel.local.LocalAddress;

import com.google.inject.AbstractModule;

public class BroadcastConstantsGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    bindConstant().annotatedWith(MaxHops.class).to(6);
    bindConstant().annotatedWith(MaxTtl.class).to(6);
    bindConstant().annotatedWith(IdSize.class).to(16);

    bind(SocketAddress.class).annotatedWith(BroadcastListeningAddress.class).toInstance(
        new LocalAddress("broadcast-example"));
  }

}

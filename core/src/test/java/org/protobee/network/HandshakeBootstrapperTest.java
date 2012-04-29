package org.protobee.network;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Set;

import org.jboss.netty.channel.ChannelPipeline;
import org.junit.Test;
import org.protobee.AbstractTest;
import org.protobee.identity.NetworkIdentity;
import org.protobee.modules.ProtocolModule;
import org.protobee.protocol.ProtocolConfig;
import org.protobee.session.SessionModel;

import com.google.common.collect.Sets;
import com.google.inject.Injector;


public class HandshakeBootstrapperTest extends AbstractTest {

  @Test
  public void testSessionSet() {

  }
}

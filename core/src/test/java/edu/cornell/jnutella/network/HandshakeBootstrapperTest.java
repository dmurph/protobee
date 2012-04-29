package edu.cornell.jnutella.network;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Set;

import org.jboss.netty.channel.ChannelPipeline;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.google.inject.Injector;

import edu.cornell.jnutella.AbstractTest;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.modules.ProtocolModule;
import edu.cornell.jnutella.protocol.ProtocolConfig;
import edu.cornell.jnutella.session.SessionModel;

public class HandshakeBootstrapperTest extends AbstractTest {

  @Test
  public void testSessionSet() {

  }
}

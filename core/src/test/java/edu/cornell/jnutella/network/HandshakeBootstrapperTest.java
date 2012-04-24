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
import edu.cornell.jnutella.identity.ProtocolIdentityModel;
import edu.cornell.jnutella.modules.ProtocolModule;
import edu.cornell.jnutella.protocol.ProtocolConfig;
import edu.cornell.jnutella.session.SessionModel;

public class HandshakeBootstrapperTest extends AbstractTest {

  @Test
  public void testSessionSet() {
    Set<ProtocolModule> modules = Sets.newHashSet();
    ProtocolConfig config = mockDefaultProtocolConfig(modules);
    ProtocolIdentityModel identityModel = config.createIdentityModel();
    when(config.createIdentityModel()).thenReturn(identityModel);

    Injector inj = getInjectorWithProtocolConfig(config);

    when(identityModel.hasCurrentSession()).thenReturn(false);

    HandshakeStateBootstrapper handshakeBootstrapper =
        inj.getInstance(HandshakeStateBootstrapper.class);

    NetworkIdentity identity = createIdentity(inj);

    SocketAddress address = new InetSocketAddress(90);
    handshakeBootstrapper.bootstrapSession(config, identity, address, null,
        mock(ChannelPipeline.class));

    verify(identityModel).setCurrentSessionModel(any(SessionModel.class));
  }
}

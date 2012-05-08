package org.protobee.gnutella;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.junit.Before;
import org.protobee.AbstractTest;
import org.protobee.ProtobeeGuiceModule;
import org.protobee.identity.NetworkIdentity;
import org.protobee.identity.NetworkIdentityManager;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolConfig;

import com.google.common.collect.Sets;
import com.google.common.net.InetAddresses;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public class AbstractGnutellaTest extends AbstractTest {

  @Before
  public void setup() {
    localTesters = Sets.newHashSet();
    injector = Guice.createInjector(new ProtobeeGuiceModule(), new GnutellaGuiceModule());
  }

  public Injector getInjector(Module overridingModule) {
    return Guice.createInjector(Modules.override(new ProtobeeGuiceModule(),
        new GnutellaGuiceModule()).with(overridingModule));
  }

  public static GnutellaServantModel initializeMe(Injector inj, int files, int size) {
    return initializeMe(inj, new InetSocketAddress(InetAddresses.forString("127.0.0.1"), 101),
        files, size);
  }

  public static GnutellaServantModel initializeMe(Injector inj, SocketAddress address, int files,
      int size) {
    NetworkIdentityManager manager = inj.getInstance(NetworkIdentityManager.class);
    NetworkIdentity me = manager.getMe();
    me.enterScope();
    GnutellaServantModel servant = inj.getInstance(GnutellaServantModel.class);
    me.exitScope();
    servant.setFileCount(files);
    servant.setFileSizeInKB(size);
    manager.setListeningAddress(me, getGnutellaProtocol(inj), address);
    return servant;
  }

  public static Protocol getGnutellaProtocol(Injector inj) {
    return inj.getInstance(Key.get(Protocol.class, Gnutella.class));
  }

  public static ProtocolConfig getGnutellaProtocolConfig(Injector inj) {
    return inj.getInstance(Key.get(ProtocolConfig.class, Gnutella.class));
  }
}

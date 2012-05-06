package org.protobee.examples.broadcast;

import org.jboss.netty.channel.local.LocalAddress;
import org.junit.Test;
import org.protobee.JnutellaServantBootstrapper;
import org.protobee.ProtobeeGuiceModule;
import org.protobee.netty.LocalNettyTester;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class BroadcastHandshakeTest extends AbstractBroadcastTest {
  @Test
  public void testHandshake() throws Exception {
    Injector inj = Guice.createInjector(new ProtobeeGuiceModule(), new BroadcastGuiceModule());

    JnutellaServantBootstrapper bootstrap = inj.getInstance(JnutellaServantBootstrapper.class);
    bootstrap.startup();

    LocalNettyTester tester = createLocalNettyTester();
    tester.connect(new LocalAddress("broadcast-example"), new LocalAddress("test"));
    basicHandshake(tester);
    tester.verifyNotClosed();

    bootstrap.shutdown(1000);
  }
  
  @Test
  public void testTimedHandshake() throws Exception {
    Injector inj = Guice.createInjector(new ProtobeeGuiceModule(), new BroadcastGuiceModule());

    JnutellaServantBootstrapper bootstrap = inj.getInstance(JnutellaServantBootstrapper.class);
    bootstrap.startup();

    LocalNettyTester tester = createLocalNettyTester();
    tester.connect(new LocalAddress("broadcast-example"), new LocalAddress("test"));
    timedHandshake(tester);
    tester.verifyNotClosed();

    bootstrap.shutdown(1000);
  }
}

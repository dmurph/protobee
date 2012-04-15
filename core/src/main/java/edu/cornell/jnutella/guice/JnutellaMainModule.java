package edu.cornell.jnutella.guice;

import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;

import edu.cornell.jnutella.gnutella.GnutellaModule;
import edu.cornell.jnutella.identity.NetworkIdentityManager;

public class JnutellaMainModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new GnutellaModule());
    install(new LogModule());

    InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

    bind(NetworkIdentityManager.class).in(Singleton.class);

  }

}

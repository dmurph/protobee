package edu.cornell.jnutella.gnutella;

import org.jboss.netty.channel.ChannelHandler;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

import edu.cornell.jnutella.gnutella.messages.decoding.DecodingModule;
import edu.cornell.jnutella.gnutella.messages.decoding.GnutellaDecoderHandler;
import edu.cornell.jnutella.gnutella.messages.encoding.EncodingModule;
import edu.cornell.jnutella.gnutella.messages.encoding.GnutellaEncoderHandler;
import edu.cornell.jnutella.gnutella.modules.PingModule;
import edu.cornell.jnutella.gnutella.session.FlowControlHandler;
import edu.cornell.jnutella.gnutella.session.GnutellaMessageReceiver;
import edu.cornell.jnutella.gnutella.session.GnutellaMessageWriter;
import edu.cornell.jnutella.gnutella.session.GnutellaSessionModel;
import edu.cornell.jnutella.gnutella.session.NoOpFlowControl;
import edu.cornell.jnutella.guice.IdentityScope;
import edu.cornell.jnutella.guice.SessionScope;
import edu.cornell.jnutella.modules.ProtocolModule;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.ProtocolConfig;

public class GnutellaGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new DecodingModule());
    install(new EncodingModule());

    bind(GnutellaSessionModel.class);

    bind(GnutellaSessionModel.class).in(SessionScope.class);
    bind(GnutellaIdentityModel.class).in(IdentityScope.class);

    Multibinder<ProtocolConfig> protocolBinder =
        Multibinder.newSetBinder(binder(), ProtocolConfig.class);
    protocolBinder.addBinding().to(GnutellaProtocolConfig.class).in(Singleton.class);


    bind(Protocol.class).annotatedWith(Gnutella.class).toProvider(GnutellaProtocolConfig.class)
        .in(Singleton.class);

    Multibinder<ProtocolModule> protocolModules =
        Multibinder.newSetBinder(binder(), ProtocolModule.class, Gnutella.class);
    protocolModules.addBinding().to(PingModule.class);
    
    bind(FlowControlHandler.class).to(NoOpFlowControl.class);

  }

  @Provides
  @Gnutella
  public ChannelHandler[] getChannelHandlers(GnutellaDecoderHandler decoder,
      GnutellaEncoderHandler encoder, FlowControlHandler flow, GnutellaMessageReceiver receiver,
      GnutellaMessageWriter writer) {
    return new ChannelHandler[] {decoder, encoder, flow, receiver, writer};
  }
}

package edu.cornell.jnutella.messages;

import static org.mockito.Mockito.*;

import java.net.SocketAddress;

import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;

import edu.cornell.jnutella.AbstractTest;
import edu.cornell.jnutella.ConnectionManager;
import edu.cornell.jnutella.messages.encoding.GnutellaEncoderHandler;
import edu.cornell.jnutella.session.SessionModel;
import edu.cornell.jnutella.session.gnutella.GnutellaSessionModel;
import edu.cornell.jnutella.session.gnutella.GnutellaSessionState;

public class EncodingHandlerTest extends AbstractTest {

  @Test
  public void testUseMessageEncoder() throws Exception {
    final HttpRequestEncoder requestEncoder = mock(HttpRequestEncoder.class);
    final HttpResponseEncoder responseEncoder = mock(HttpResponseEncoder.class);
    final ConnectionManager manager = mock(ConnectionManager.class);

    Injector injector = getInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(HttpRequestEncoder.class).toInstance(requestEncoder);
        bind(HttpResponseEncoder.class).toInstance(responseEncoder);
        bind(ConnectionManager.class).toInstance(manager);
      }
    });

    GnutellaEncoderHandler encoderHandler = injector.getInstance(GnutellaEncoderHandler.class);
    GnutellaSessionModel model = new GnutellaSessionModel(null);
    model.setState(GnutellaSessionState.MESSAGES);
    when(manager.getSessionModel(any(SocketAddress.class))).thenReturn(model);

    // TODO
  }
}

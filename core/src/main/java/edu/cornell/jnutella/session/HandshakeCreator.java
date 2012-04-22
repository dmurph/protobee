package edu.cornell.jnutella.session;

import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;

import com.google.inject.Inject;

import edu.cornell.jnutella.guice.SessionScope;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.headers.CompatabilityHeaderMerger;

@SessionScope
public class HandshakeCreator {

  private final CompatabilityHeaderMerger merger;
  private final Protocol protocol;

  @Inject
  public HandshakeCreator(CompatabilityHeaderMerger merger, Protocol protocol) {
    this.merger = merger;
    this.protocol = protocol;
  }

  public HttpRequest createHandshakeRequest() {
    HttpRequest request =
        new DefaultHttpRequest(new HttpVersion(protocol.name(), protocol.majorVersion(),
            protocol.minorVersion(), true), new HttpMethod("CONNECT"), "");
    merger.mergeHeaders(request);

    return request;
  }
}

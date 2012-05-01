package org.protobee.session.handshake;

import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.protobee.guice.SessionScope;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.headers.CompatabilityHeaderMerger;

import com.google.inject.Inject;

/**
 * Creates the handshake http request, and populates the merged compatability headers of the
 * modules. The http version is the name, major version, and minor version from the protocol
 * 
 * @author Daniel
 */
@SessionScope
public class HandshakeCreator {

  private final CompatabilityHeaderMerger merger;
  private final Protocol protocol;

  @Inject
  public HandshakeCreator(CompatabilityHeaderMerger merger, Protocol protocol) {
    this.merger = merger;
    this.protocol = protocol;
  }

  public HttpRequest createHandshakeRequest(HttpMethod method, String uri) {
    HttpRequest request =
        new DefaultHttpRequest(new HttpVersion(protocol.name(), protocol.majorVersion(),
            protocol.minorVersion(), true), method, uri);
    merger.mergeHeaders(request);

    return request;
  }
}

package org.protobee.session;

import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.protobee.guice.SessionScope;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.headers.CompatabilityHeaderMerger;

import com.google.inject.Inject;


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

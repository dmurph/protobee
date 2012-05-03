package org.protobee.session.handshake;

import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.protobee.compatability.ModuleCompatabilityVersionMerger;
import org.protobee.guice.scopes.ProtocolScope;
import org.protobee.protocol.Protocol;

import com.google.inject.Inject;

/**
 * Creates the handshake http request, and populates the merged compatability headers of the
 * modules. The http version is the name, major version, and minor version from the protocol
 * 
 * @author Daniel
 */
@ProtocolScope
public class HandshakeCreator {

  private final ModuleCompatabilityVersionMerger merger;

  @Inject
  public HandshakeCreator(ModuleCompatabilityVersionMerger merger) {
    this.merger = merger;
  }

  public HttpRequest createHandshakeRequest(Protocol protocol, HttpMethod method, String uri) {
    HttpRequest request =
        new DefaultHttpRequest(new HttpVersion(protocol.name(), protocol.majorVersion(),
            protocol.minorVersion(), true), method, uri);
    merger.populateModuleHeaders(request);

    return request;
  }
}

package org.protobee.protocol.headers;

import java.util.Map;

import org.jboss.netty.handler.codec.http.HttpMessage;
import org.protobee.compatability.VersionRangeMerger;
import org.protobee.guice.scopes.SessionScope;
import org.protobee.session.SessionProtocolModules;
import org.protobee.util.VersionComparator;

@SessionScope
public class SessionHeaderFilter {

  private final SessionProtocolModules modules;
  private final VersionComparator comp;
  private final VersionRangeMerger merger;

  public SessionHeaderFilter(VersionComparator comp, VersionRangeMerger merger,
      SessionProtocolModules modules) {
    this.modules = modules;
    this.comp = comp;
    this.merger = merger;
  }
  
  public Map<String, String> mergeHeaders(HttpMessage message) {
    
  }
}

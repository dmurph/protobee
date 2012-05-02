package org.protobee.gnutella.session;

import java.util.Map;
import java.util.Set;

import org.jboss.netty.handler.codec.http.HttpMessage;
import org.protobee.gnutella.Gnutella;
import org.protobee.guice.scopes.SessionScope;
import org.protobee.modules.ProtocolModule;
import org.protobee.session.SessionProtocolModules;
import org.protobee.util.HeaderUtil;

import com.google.common.collect.Sets;
import com.google.inject.Inject;


@SessionScope
public class GnutellaSessionModel implements SessionProtocolModules {

  private Map<String, String> allHeaders = null;
  private final Set<ProtocolModule> mutableModules;

  @Inject
  public GnutellaSessionModel(@Gnutella Set<ProtocolModule> modules) {
    this.mutableModules = Sets.newHashSet(modules);
  }

  /**
   * Parse headers from http message. in particular, concatenates duplicate headers
   * 
   * @param message
   */
  void setHeaders(HttpMessage message) {
    allHeaders = HeaderUtil.mergeDuplicatesWithComma(message);
  }

  public Map<String, String> getAllHeaders() {
    return allHeaders;
  }

  @Override
  public Set<ProtocolModule> getMutableModules() {
    return mutableModules;
  }
}

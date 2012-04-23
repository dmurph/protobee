package edu.cornell.jnutella.gnutella.session;

import java.util.Map;
import java.util.Set;

import org.jboss.netty.handler.codec.http.HttpMessage;

import com.google.common.collect.Sets;
import com.google.inject.Inject;

import edu.cornell.jnutella.gnutella.Gnutella;
import edu.cornell.jnutella.guice.SessionScope;
import edu.cornell.jnutella.modules.ProtocolModule;
import edu.cornell.jnutella.session.ProtocolSessionModel;
import edu.cornell.jnutella.util.HeaderUtil;

@SessionScope
public class GnutellaSessionModel implements ProtocolSessionModel {

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
    allHeaders = HeaderUtil.mergedGnutellaHeaders(message);
  }

  public Map<String, String> getAllHeaders() {
    return allHeaders;
  }

  @Override
  public Set<ProtocolModule> getMutableModules() {
    return mutableModules;
  }
}

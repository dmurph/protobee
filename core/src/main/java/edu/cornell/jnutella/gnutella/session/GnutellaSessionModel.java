package edu.cornell.jnutella.gnutella.session;

import java.util.Map;
import java.util.Set;

import org.jboss.netty.handler.codec.http.HttpMessage;

import com.google.common.collect.Sets;
import com.google.inject.Inject;

import edu.cornell.jnutella.gnutella.Gnutella;
import edu.cornell.jnutella.guice.SessionScope;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.modules.ProtocolModule;
import edu.cornell.jnutella.protocol.session.SessionModel;
import edu.cornell.jnutella.util.HeaderUtil;

@SessionScope
public class GnutellaSessionModel extends SessionModel {

  private GnutellaSessionState state;
  private Map<String, String> allHeaders = null;

  @Inject
  public GnutellaSessionModel(NetworkIdentity identity, @Gnutella Set<ProtocolModule> modules) {
    super(identity, Sets.newHashSet(modules));
  }

  public GnutellaSessionState getState() {
    return state;
  }

  public void setState(GnutellaSessionState state) {
    this.state = state;
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
}

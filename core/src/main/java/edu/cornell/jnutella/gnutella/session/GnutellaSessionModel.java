package edu.cornell.jnutella.gnutella.session;

import java.util.Map;
import java.util.Set;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.HttpMessage;

import com.google.common.eventbus.EventBus;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import edu.cornell.jnutella.gnutella.GnutellaIdentityModel;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.modules.ProtocolModule;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.session.SessionModel;
import edu.cornell.jnutella.util.HeaderUtil;

public class GnutellaSessionModel extends SessionModel {

  public static interface Factory {
    GnutellaSessionModel create(Channel channel, Protocol protocol, NetworkIdentity identity,
        Set<ProtocolModule> mutableModules);
  }

  private GnutellaSessionState state;
  private Map<String, String> allHeaders = null;

  @AssistedInject
  public GnutellaSessionModel(@Assisted Channel channel, @Assisted Protocol protocol,
      @Assisted NetworkIdentity identity, EventBus eventBus,
      @Assisted Set<ProtocolModule> mutableModules) {
    super(channel, protocol, identity, eventBus, mutableModules);
  }

  public GnutellaSessionState getState() {
    return state;
  }

  public void setState(GnutellaSessionState state) {
    this.state = state;
  }

  public GnutellaIdentityModel getProtocolModel() {
    return (GnutellaIdentityModel) super.getProtocolModel();
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

package edu.cornell.jnutella;

import java.net.SocketAddress;
import java.util.Map;

import com.google.common.collect.Maps;

import edu.cornell.jnutella.session.SessionModel;

public class ConnectionManager {

  private final Map<SocketAddress, SessionModel> sessionMap = Maps.newHashMap();

  public ConnectionManager() {

  }


  public SessionModel getSessionModel(SocketAddress address) {
    return sessionMap.get(address);
  }

  public boolean hasSessionModel(SocketAddress address) {
    return sessionMap.containsKey(address);
  }

  public void addSessionModel(SocketAddress address, SessionModel model) {
    sessionMap.put(address, model);
  }
}

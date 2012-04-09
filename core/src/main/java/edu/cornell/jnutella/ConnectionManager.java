package edu.cornell.jnutella;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.session.SessionModel;

public class ConnectionManager {

  private final Map<ConnectionKey, SessionModel> sessionMap = Maps.newHashMap();
  private final Multimap<Protocol, SocketAddress> protocolOptions = Multimaps
      .synchronizedSetMultimap(HashMultimap.<Protocol, SocketAddress>create());

  private final Object sessionMapLock = new Object();

  public ConnectionManager() {

  }

  public SessionModel getSession(ConnectionKey address) {
    synchronized (sessionMapLock) {
      return sessionMap.get(address);
    }
  }

  public boolean hasSession(ConnectionKey address) {
    synchronized (sessionMapLock) {
      return sessionMap.containsKey(address);
    }
  }

  public void addSession(ConnectionKey address, SessionModel model) {
    synchronized (sessionMapLock) {
      sessionMap.put(address, model);
    }
  }
  
  public SessionModel removeSession(ConnectionKey key) {
    synchronized (sessionMapLock) {
      return sessionMap.remove(key);
    }
  }

  public void addProtocolOption(Protocol protocol, SocketAddress remoteAddress) {
    protocolOptions.put(protocol, remoteAddress);
  }

  public Collection<SocketAddress> getProtocolOptions(Protocol protocol) {
    // this returns a modifiable collection that can modify the multimap...
    return protocolOptions.get(protocol);
  }

  public void removeProtocolOption(Protocol protocol, SocketAddress remoteAddress) {
    protocolOptions.remove(protocol, remoteAddress);
  }
}

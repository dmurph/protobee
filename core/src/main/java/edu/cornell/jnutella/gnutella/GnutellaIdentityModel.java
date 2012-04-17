package edu.cornell.jnutella.gnutella;

import java.net.SocketAddress;

import edu.cornell.jnutella.gnutella.session.GnutellaSessionModel;
import edu.cornell.jnutella.identity.ProtocolIdentityModel;
import edu.cornell.jnutella.protocol.session.SessionModel;

public class GnutellaIdentityModel implements ProtocolIdentityModel {

  
  private SocketAddress address;
  private byte[] guid;
  private GnutellaSessionModel currentSession;

  @Override
  public void setNetworkAddress(SocketAddress address) {
    this.address = address;
  }

  @Override
  public SocketAddress getNetworkAddress() {
    return address;
  }

  public byte[] getGuid() {
    return guid;
  }

  public void setGuid(byte[] guid) {
    this.guid = guid;
  }
  
  public boolean hasCurrentSession() { 
    return currentSession != null;
  }

  public GnutellaSessionModel getCurrentSession() {
    return currentSession;
  }

  @Override
  public void clearCurrentSession() {
    currentSession = null;
  }

  @Override
  public void setCurrentSessionModel(SessionModel model) {
    this.currentSession = (GnutellaSessionModel) model;
  }
}

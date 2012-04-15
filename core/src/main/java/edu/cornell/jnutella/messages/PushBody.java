package edu.cornell.jnutella.messages;

import java.net.InetAddress;

import javax.annotation.Nullable;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import edu.cornell.jnutella.extension.GGEP;
import edu.cornell.jnutella.extension.GGEPKeys;
import edu.cornell.jnutella.messages.decoding.DecodingException;
import edu.cornell.jnutella.util.GUID;

public class PushBody implements MessageBody {

  public static final long FW_TRANS_INDEX = Integer.MAX_VALUE - 2;
  private GUID servantID;
  private long index;
  private InetAddress address;
  private int port;
  private GGEP ggep;
  
  @AssistedInject
  public PushBody(@Assisted GUID servantID, @Assisted long index,
                  @Assisted InetAddress address, @Assisted int port,
                  @Nullable @Assisted GGEP ggep) throws DecodingException {
    this.servantID = servantID;
    this.index = index;
    this.address = address;
    this.port = port;
    this.ggep = ggep;
  }
  
  public GUID getServantID() {
    return servantID;
  }
  
  public long getIndex() {
    return index;
  }

  public InetAddress getAddress() {
    return address;
  }

  public int getPort() {
    return port;
  }

  public GGEP getGgep() {
    return ggep;
  }

  public boolean isTLSCapable() {
    if(ggep != null && ggep != new GGEP()) {
      return ggep.hasKey(GGEPKeys.GGEP_HEADER_TLS_CAPABLE);
    } else {
      return false;
    }
  }

  public boolean isFirewallTransferPush() {
    return (getIndex() == FW_TRANS_INDEX);
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((address == null) ? 0 : address.hashCode());
    result = prime * result + ((ggep == null) ? 0 : ggep.hashCode());
    result = prime * result + (int) (index ^ (index >>> 32));
    result = prime * result + port;
    result = prime * result + ((servantID == null) ? 0 : servantID.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    PushBody other = (PushBody) obj;
    if (address == null) {
      if (other.address != null) return false;
    } else if (!address.equals(other.address)) return false;
    if (ggep == null) {
      if (other.ggep != null) return false;
    } else if (!ggep.equals(other.ggep)) return false;
    if (index != other.index) return false;
    if (port != other.port) return false;
    if (servantID == null) {
      if (other.servantID != null) return false;
    } else if (!servantID.equals(other.servantID)) return false;
    return true;
  }
}




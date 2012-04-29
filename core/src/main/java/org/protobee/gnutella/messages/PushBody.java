package org.protobee.gnutella.messages;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;

import javax.annotation.Nullable;

import org.protobee.extension.GGEP;
import org.protobee.extension.GGEPKeys;
import org.protobee.gnutella.messages.decoding.DecodingException;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;


public class PushBody implements MessageBody {

  public static final long FW_TRANS_INDEX = Integer.MAX_VALUE - 2;
  private byte[] servantID;
  private long index;
  private InetSocketAddress socketAddress;
  private int port;
  private GGEP ggep;

  @AssistedInject
  public PushBody(@Assisted byte[] servantID, @Assisted long index,
                  @Assisted InetSocketAddress socketAddress, 
                  @Nullable @Assisted GGEP ggep) throws DecodingException {
    this.servantID = servantID;
    this.index = index;
    this.socketAddress = socketAddress;
    this.ggep = ggep;
  }

  public byte[] getServantID() {
    return servantID;
  }

  public long getIndex() {
    return index;
  }
  
  public InetSocketAddress getsSocketAddress(){
    return socketAddress;
  }

  public InetAddress getAddress() {
    return socketAddress.getAddress();
  }

  public int getPort() {
    return socketAddress.getPort();
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
    result = prime * result + ((ggep == null) ? 0 : ggep.hashCode());
    result = prime * result + (int) (index ^ (index >>> 32));
    result = prime * result + port;
    result = prime * result + Arrays.hashCode(servantID);
    result = prime * result + ((socketAddress == null) ? 0 : socketAddress.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    PushBody other = (PushBody) obj;
    if (ggep == null) {
      if (other.ggep != null) return false;
    } else if (!ggep.equals(other.ggep)) return false;
    if (index != other.index) return false;
    if (port != other.port) return false;
    if (!Arrays.equals(servantID, other.servantID)) return false;
    if (socketAddress == null) {
      if (other.socketAddress != null) return false;
    } else if (!socketAddress.equals(other.socketAddress)) return false;
    return true;
  }

}




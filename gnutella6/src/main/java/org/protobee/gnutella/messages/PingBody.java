package org.protobee.gnutella.messages;

import javax.annotation.Nullable;

import org.protobee.annotation.InjectLogger;
import org.protobee.gnutella.extension.BadGGEPPropertyException;
import org.protobee.gnutella.extension.GGEP;
import org.protobee.gnutella.extension.GGEPKeys;
import org.slf4j.Logger;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;


public class PingBody implements MessageBody {

  /** Mask for where leaf/ultrapeer requests are. */
  public static final byte SCP_ULTRAPEER_OR_LEAF_MASK = 0x1;

  /** If we're requesting leaf hosts. */
  public static final byte SCP_LEAF = 0x0;

  /** If we're requesting ultrapeer hosts. */
  public static final byte SCP_ULTRAPEER = 0x1;

  /** If we support incoming TLS. */
  public static final byte SCP_TLS = 0x2;

  private final GGEP ggep;

  @InjectLogger
  private Logger log;

  @AssistedInject
  public PingBody(@Nullable @Assisted("ggep") GGEP ggep) {
    this.ggep = (ggep == null || ggep.isEmpty()) ? null : ggep;
  }

  public GGEP getGgep() {
    return ggep;
  }

  @Override
  public String toString() {
    return "PingRequest(" + super.toString() + ")";
  }

  /**
   * Accessor for whether or not this ping meets the criteria for being a "heartbeat" ping, namely
   * having ttl=0 and hops=1.
   * 
   * @return <tt>true</tt> if this ping apears to be a "heartbeat" ping, otherwise <tt>false</tt>
   */
  public boolean isHeartbeat(MessageHeader header) {
    return (header.getHops() == 1 && header.getTtl() == 0);
  }

  /**
   * Marks this ping request as requesting a pong carrying an ip:port info.
   */
  public void addIPRequest() {
    if (ggep != null) {
      ggep.put(GGEPKeys.GGEP_HEADER_IPPORT);
    }
  }

  /**
   * Determines if this PingRequest has the 'supports cached pongs' marking.
   */
  public boolean supportsCachedPongs() {
    return (ggep == null) ? false : ggep.hasKey(GGEPKeys.GGEP_HEADER_SUPPORT_CACHE_PONGS);
  }

  /**
   * Gets the data value for the SCP field, if one exists. If none exist, null is returned. Else, a
   * byte[] of some size is returned.
   */
  public byte[] getSupportsCachedPongData() {
    if (ggep == null) { return null; }
    if (ggep.hasKey(GGEPKeys.GGEP_HEADER_SUPPORT_CACHE_PONGS)) {
      try {
        return ggep.getBytes(GGEPKeys.GGEP_HEADER_SUPPORT_CACHE_PONGS);
      } catch (BadGGEPPropertyException e) {
        log.error("could not ready ggep block");
        return null;
      }
    }
    return null;
  }

  public boolean isQueryKeyRequest(MessageHeader header) {
    if (!(header.getTtl() == 0) || !(header.getHops() == 1)) return false;

    return (ggep == null) ? false : ggep.hasKey(GGEPKeys.GGEP_HEADER_QUERY_KEY_SUPPORT);
  }

  /**
   * @return whether this ping wants a reply carrying IP:Port info.
   */
  public boolean requestsIP() {
    return (ggep == null) ? false : ggep.hasKey(GGEPKeys.GGEP_HEADER_IPPORT);
  }

  /**
   * @return whether this ping wants a reply carrying DHT IPP info
   */
  public boolean requestsDHTIPP() {
    return (ggep == null) ? false : ggep.hasKey(GGEPKeys.GGEP_HEADER_DHT_IPPORTS);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((ggep == null) ? 0 : ggep.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    PingBody other = (PingBody) obj;
    if (ggep == null) {
      if (other.ggep != null) return false;
    } else if (!ggep.equals(other.ggep)) return false;
    return true;
  }
}

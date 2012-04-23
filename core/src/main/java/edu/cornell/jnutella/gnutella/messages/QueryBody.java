package edu.cornell.jnutella.gnutella.messages;

import javax.annotation.Nullable;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import edu.cornell.jnutella.extension.GGEP;
import edu.cornell.jnutella.extension.HUGEExtension;
import edu.cornell.jnutella.gnutella.routing.DynamicQueryConstants;
import edu.cornell.jnutella.util.ByteUtils;

public class QueryBody implements MessageBody {
  /**
   * MINSPEED_*_BIT - these are the bit numbers for the new meanings of MinSpeed bits.
   */
  public static final int MINSPEED_BITBASED_BIT = 15;
  public static final int MINSPEED_FIREWALL_BIT = 14;
  public static final int MINSPEED_XML_BIT = 13;

  /**
   * The legacy Minimum speed semantic has been deprecated. As a legacy servent should never send a
   * very high value for the minimum speed, the higher bit (bit 15) is used as a flag to detect
   * queries with the new semantic. If the bit 15 is null, then this is a query with the legacy
   * minspeed semantic. If the bit 15 is set to 1, then this is a query with the new minimum speed
   * semantic.
   * 
   * In the new semantic, each bit is used as a flag, mostly to indicate compatibility with new
   * gnutella extensions. The affectation of each bit is as follow :
   * 
   * Bit 14 : Firewalled indicator. This flag can be used by the remote servent to avoid returning
   * queryHits if it is itself firewalled, as the requesting servent won't be able to download the
   * files.
   * 
   * Bit 13 : XML Metadata. Set this bit to 1 if you want the servent to receive XML Metadata. This
   * flag has been set to spare bandwidth, returning metadata in queryHits only if the requester
   * asks for it.
   * 
   * Bit 12 : Leaf Guided Dynamic Query. When the bit is set to 1, this means that the query is sent
   * by a leaf which wants to control the dynamic query mechanism. This is part of the Leaf guidance
   * of dynamic queries proposal. This information is only used by the ultrapeers shileding this
   * leave if they implement leaf guidance of dynamic queries.
   * 
   * Bit 11 : GGEP "H" allowed. If this bit is set to 1, then the sender is able to parse the GGEP
   * "H" extension which is a replacement for the leagacy HUGE GEM extension. This is meant to start
   * replacing the GEM mecanism with GGEP extensions, as GEM extensions are now deprecated.
   * 
   * Bit 10 : Out of Band Query. This flag is used to recognize a Query which was sent using the Out
   * Of Band query extension.
   * 
   * Bits 0-9 : reserved for a future use.
   */
  private final short minSpeed;

  /**
   * The query string.
   */
  private final String query;

  private final GGEP ggep;
  private final HUGEExtension huge;

  @AssistedInject
  public QueryBody(@Assisted short minSpeed, @Assisted String query, @Nullable @Assisted GGEP ggep, @Nullable @Assisted HUGEExtension huge) {
    this.minSpeed = minSpeed;
    this.query = query;
    this.ggep = ggep;
    this.huge = huge;
  }
  
  public boolean hasInvalidQuery(){
    return query.length() < DynamicQueryConstants.MIN_SEARCH_TERM_LENGTH;
  }
  
  public short getMinSpeed(){
    return minSpeed;
  }

  public String getQuery() {
    return query;
  }

  public GGEP getGgep() {
    return ggep;
  }
  
  public HUGEExtension getHuge(){
    return huge;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((ggep == null) ? 0 : ggep.hashCode());
    result = prime * result + ((huge == null) ? 0 : huge.hashCode());
    result = prime * result + minSpeed;
    result = prime * result + ((query == null) ? 0 : query.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    QueryBody other = (QueryBody) obj;
    if (ggep == null) {
      if (other.ggep != null) return false;
    } else if (!ggep.equals(other.ggep)) return false;
    if (huge == null) {
      if (other.huge != null) return false;
    } else if (!huge.equals(other.huge)) return false;
    if (minSpeed != other.minSpeed) return false;
    if (query == null) {
      if (other.query != null) return false;
    } else if (!query.equals(other.query)) return false;
    return true;
  }

  public boolean isMinSpeedBitBased() {
    return ByteUtils.isBitSet(minSpeed, MINSPEED_BITBASED_BIT);
  }

  public boolean isRequesterFirewalled() {
    return ByteUtils.isBitSet(minSpeed, MINSPEED_FIREWALL_BIT);
  }

  public boolean isXMLResultsCapable() {
    return ByteUtils.isBitSet(minSpeed, MINSPEED_XML_BIT);
  }
}

package edu.cornell.jnutella.gnutella.routing;

import edu.cornell.jnutella.util.GUID;
import edu.cornell.jnutella.util.URN;

public class IdentityHash {
  private GUID guid;
  private int urnHash;

  public IdentityHash(GUID guid, UrnSet urns){
    this.guid = guid;
    this.urnHash = urns.hashCode();
  }
  
  public IdentityHash(GUID guid, URN[] urns){
    this.guid = guid;
    this.urnHash = (new UrnSet(urns)).hashCode();
  }

  public GUID getGuid() {
    return guid;
  }

  public int getUrnHash() {
    return urnHash;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((guid == null) ? 0 : guid.hashCode());
    result = prime * result + urnHash;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    IdentityHash other = (IdentityHash) obj;
    if (guid == null) {
      if (other.guid != null) return false;
    } else if (!guid.equals(other.guid)) return false;
    if (urnHash != other.urnHash) return false;
    return true;
  }
  
}

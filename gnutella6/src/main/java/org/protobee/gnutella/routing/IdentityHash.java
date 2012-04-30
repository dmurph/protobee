package org.protobee.gnutella.routing;

import java.util.Arrays;

import org.protobee.gnutella.util.URN;


public class IdentityHash {
  private byte[] guid;
  private int urnHash;

  public IdentityHash(byte[] guid, UrnSet urns){
    this.guid = guid;
    this.urnHash = urns.hashCode();
  }
  
  public IdentityHash(byte[] guid, URN[] urns){
    this.guid = guid;
    this.urnHash = (new UrnSet(urns)).hashCode();
  }

  public byte[] getGuid() {
    return guid;
  }

  public int getUrnHash() {
    return urnHash;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(guid);
    result = prime * result + urnHash;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    IdentityHash other = (IdentityHash) obj;
    if (!Arrays.equals(guid, other.guid)) return false;
    if (urnHash != other.urnHash) return false;
    return true;
  }
  
}

package org.protobee.gnutella.routing;

import java.util.Arrays;

import org.protobee.gnutella.util.URN;


public class UrnSet {
  
  private URN[] urnSet;
  
  public UrnSet(URN[] urnSet){
    this.urnSet = urnSet;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(urnSet);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    UrnSet other = (UrnSet) obj;
    if (!Arrays.equals(urnSet, other.urnSet)) return false;
    return true;
  }
  
  

}

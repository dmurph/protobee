package org.protobee.gnutella.extension;

import java.util.Arrays;

import org.protobee.gnutella.util.URN;


public class HUGEExtension {

  private URN[] urns;

  public HUGEExtension(URN[] urns){
    this.urns = (urns == null) ? new URN[0] : urns;
  }

  public URN[] getUrns() {
    return urns;
  }
  
  public boolean isEmpty(){
    return (urns.length == 0);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(urns);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    HUGEExtension other = (HUGEExtension) obj;
    if (!Arrays.equals(urns, other.urns)) return false;
    return true;
  }

}


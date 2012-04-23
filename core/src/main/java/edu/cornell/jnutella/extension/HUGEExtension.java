package edu.cornell.jnutella.extension;

import java.util.Arrays;

import edu.cornell.jnutella.util.URN;

public class HUGEExtension {

  private URN[] urns;
  private GGEP ggep;

  public HUGEExtension(URN[] urns, GGEP ggep){
    this.urns = (urns != null) ? urns : new URN[0];
    this.ggep = ggep;
  }

  public URN[] getUrns() {
    return urns;
  }
  
  public GGEP getGGEP(){
    return ggep;
  }
  
  public boolean isEmpty(){
    if (ggep != null || ggep.getHeaders().size() == 0) return false;
    if (urns.length != 0) return false;
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((ggep == null) ? 0 : ggep.hashCode());
    result = prime * result + Arrays.hashCode(urns);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    HUGEExtension other = (HUGEExtension) obj;
    if (ggep == null) {
      if (other.ggep != null) return false;
    } else if (!ggep.equals(other.ggep)) return false;
    if (!Arrays.equals(urns, other.urns)) return false;
    return true;
  }

}


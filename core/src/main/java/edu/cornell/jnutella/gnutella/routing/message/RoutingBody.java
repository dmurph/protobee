package edu.cornell.jnutella.gnutella.routing.message;

import edu.cornell.jnutella.gnutella.messages.MessageBody;

public abstract class RoutingBody implements MessageBody {
  
  public static final byte RESET_TABLE_VARIANT = 0x00;
  public static final byte PATCH_TABLE_VARIANT = 0x01;
  protected byte variant;
  
  public RoutingBody(byte variant) {
    this.variant = variant;
  }
  
  public byte getVariant(){
    return variant;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + variant;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    RoutingBody other = (RoutingBody) obj;
    if (variant != other.variant) return false;
    return true;
  }
  
}

package edu.cornell.jnutella.gnutella.routing.message;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;


public class ResetBody extends RoutingBody {

  private long tableLength;
  private byte infinity;
  
  @AssistedInject
  public ResetBody(@Assisted long tableLength, @Assisted byte infinity){
    super(RESET_TABLE_VARIANT);
    this.tableLength = tableLength;
    this.infinity = infinity;
  }

  public byte getVariant() {
    return variant;
  }

  public long getTableLength() {
    return tableLength;
  }

  public byte getInfinity() {
    return infinity;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + infinity;
    result = prime * result + (int) (tableLength ^ (tableLength >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (getClass() != obj.getClass()) return false;
    ResetBody other = (ResetBody) obj;
    if (infinity != other.infinity) return false;
    if (tableLength != other.tableLength) return false;
    return true;
  }
  
}

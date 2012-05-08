package org.protobee.gnutella.util;

public final class URN  {

  private String urnString;
  private Type urnType;


  public URN(String urnString, Type urnType) {
    this.urnString = urnString;
    this.urnType = urnType;
  }

  public URN(String urnString) {
    
    String[] urn = urnString.split(":");
    this.urnString = urnString;
    urnType = null;

    if (urn.length >= 2){
      if (urn[1].equals("Invalid")){
        urnType = Type.INVALID;
      }
    }

    if (urn.length == 3){
      for (Type type: Type.values()){
        if (urn[1].equals(type.getDescriptor()) && (urn[2].length() == type.getLength())){
          this.urnType = type;
        }
      }
    }

    if (!urn[0].equals("urn") || urnType == null){
      urnType = Type.ANY_TYPE;
    }
  }

  public String getUrnString(){
    return urnString;
  }

  public Type getUrnType() {
    return urnType;
  }

  @Override
  public String toString() {
    return urnString;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((urnString == null) ? 0 : urnString.hashCode());
    result = prime * result + ((urnType == null) ? 0 : urnType.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    URN other = (URN) obj;
    if (urnString == null) {
      if (other.urnString != null) return false;
    } else if (!urnString.equals(other.urnString)) return false;
    if (urnType != other.urnType) return false;
    return true;
  }




  /** The range of all types for URNs. */
  public static enum Type {        
    SHA1("sha1",32),
    BITPRINT("bitprint",72),
    TTROOT("ttroot",39),
    ANY_TYPE("",-1),
    INVALID("Invalid",-1),
    GUID("guid",32);

    public static final String URN_NAMESPACE_ID = "urn:";
    private final String descriptor;
    private final int length;

    private Type(String descriptor, int length) {
      this.descriptor = descriptor;
      this.length = length;
    }

    public String getDescriptor() {
      return descriptor;
    }

    public int getLength(){
      return length;
    }

  }


}

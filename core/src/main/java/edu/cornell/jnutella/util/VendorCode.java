package edu.cornell.jnutella.util;

import java.util.Arrays;

public class VendorCode {
  
  char[] vendorCode;
  
  public VendorCode(char A, char B, char C, char D){
    vendorCode = new char[4];
    vendorCode[0] = Character.toUpperCase(A);
    vendorCode[1] = Character.toUpperCase(B);
    vendorCode[2] = Character.toUpperCase(C);
    vendorCode[3] = Character.toUpperCase(D);
  }
  
  public VendorCode(byte A, byte B, byte C, byte D){
    vendorCode = new char[4];
    vendorCode[0] = Character.toUpperCase((char) A);
    vendorCode[1] = Character.toUpperCase((char) B);
    vendorCode[2] = Character.toUpperCase((char) C);
    vendorCode[3] = Character.toUpperCase((char) D);
  }
  
  public char[] getVendorCode(){
    return vendorCode;
  }
  
  public byte[] getBytes(){
    byte[] bytes = new byte[4];
    bytes[0] = (byte) vendorCode[0];
    bytes[1] = (byte) vendorCode[1];
    bytes[2] = (byte) vendorCode[2];
    bytes[3] = (byte) vendorCode[3];
    return bytes;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(vendorCode);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    VendorCode other = (VendorCode) obj;
    if (!Arrays.equals(vendorCode, other.vendorCode)) return false;
    return true;
  }
  
}

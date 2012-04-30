package org.protobee.gnutella.messages;

import org.protobee.gnutella.util.VendorCode;

public class EQHDBody {

  private VendorCode vendorCode;
  private byte openDataSize;
  private byte flags;
  private byte controls;
  private short xmlSize;

  public EQHDBody(VendorCode vendorCode, short xmlSize, byte flags, byte controls){
    this.vendorCode = vendorCode;
    this.openDataSize = (xmlSize == 0) ? (byte) 2 : (byte) 4;
    this.flags = flags;
    this.controls = controls;
    this.xmlSize = xmlSize;
  }

  public VendorCode getVendorCode() {
    return vendorCode;
  }

  public byte getOpenDataSize() {
    return openDataSize;
  }

  public byte getFlags() {
    return flags;
  }

  public byte getControls() {
    return controls;
  }

  public short getXmlSize() {
    return xmlSize;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + controls;
    result = prime * result + flags;
    result = prime * result + openDataSize;
    result = prime * result + ((vendorCode == null) ? 0 : vendorCode.hashCode());
    result = prime * result + xmlSize;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    EQHDBody other = (EQHDBody) obj;
    if (controls != other.controls) return false;
    if (flags != other.flags) return false;
    if (openDataSize != other.openDataSize) return false;
    if (vendorCode == null) {
      if (other.vendorCode != null) return false;
    } else if (!vendorCode.equals(other.vendorCode)) return false;
    if (xmlSize != other.xmlSize) return false;
    return true;
  }


}

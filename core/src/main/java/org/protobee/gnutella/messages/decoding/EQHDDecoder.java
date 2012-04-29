package org.protobee.gnutella.messages.decoding;

import org.jboss.netty.buffer.ChannelBuffer;
import org.protobee.gnutella.messages.EQHDBody;
import org.protobee.util.ByteUtils;
import org.protobee.util.VendorCode;


public class EQHDDecoder implements PartDecoder<EQHDBody> {
 
  @Override
  public EQHDBody decode(ChannelBuffer buffer) throws DecodingException {
    VendorCode vendorCode = new VendorCode(buffer.readByte(), buffer.readByte(), buffer.readByte(), buffer.readByte());
    byte openDataSize = buffer.readByte();
    byte flags = buffer.readByte();
    byte controls = buffer.readByte();
    short xmlSize = 0;
    if (openDataSize > 2) xmlSize = (ByteUtils.leb2short(buffer));
    return new EQHDBody(vendorCode, xmlSize, flags, controls);
  }
}
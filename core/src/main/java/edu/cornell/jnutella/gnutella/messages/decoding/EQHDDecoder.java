package edu.cornell.jnutella.gnutella.messages.decoding;

import org.jboss.netty.buffer.ChannelBuffer;

import edu.cornell.jnutella.gnutella.messages.EQHDBody;
import edu.cornell.jnutella.util.ByteUtils;
import edu.cornell.jnutella.util.VendorCode;

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
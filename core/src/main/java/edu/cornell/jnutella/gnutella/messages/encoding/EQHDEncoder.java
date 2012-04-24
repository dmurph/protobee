package edu.cornell.jnutella.gnutella.messages.encoding;

import org.jboss.netty.buffer.ChannelBuffer;

import edu.cornell.jnutella.gnutella.messages.EQHDBody;
import edu.cornell.jnutella.util.ByteUtils;

public class EQHDEncoder implements PartEncoder<EQHDBody> {
  
  @Override
  public void encode(ChannelBuffer buffer, EQHDBody toEncode) throws EncodingException{
    buffer.writeBytes(toEncode.getVendorCode().getBytes());
    buffer.writeByte(toEncode.getOpenDataSize());
    buffer.writeByte(toEncode.getFlags());
    buffer.writeByte(toEncode.getControls());
    if (toEncode.getXmlSize() > 0){
      ByteUtils.short2leb((short) toEncode.getXmlSize(), buffer);
    }
  }

}

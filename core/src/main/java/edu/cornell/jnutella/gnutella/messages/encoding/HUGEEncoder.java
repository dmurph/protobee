package edu.cornell.jnutella.gnutella.messages.encoding;

import java.io.UnsupportedEncodingException;

import org.jboss.netty.buffer.ChannelBuffer;

import com.google.inject.Inject;

import edu.cornell.jnutella.extension.HUGEExtension;
import edu.cornell.jnutella.gnutella.messages.encoding.GGEPEncoder.EncoderInput;
import edu.cornell.jnutella.util.URN;

public class HUGEEncoder implements PartEncoder<HUGEExtension> {
  private final GGEPEncoder ggepEncoder;

  @Inject
  public HUGEEncoder(GGEPEncoder ggepEncoder) {
    this.ggepEncoder = ggepEncoder;
  }
  
  @Override
  public void encode(ChannelBuffer buffer, HUGEExtension toEncode) throws EncodingException {
    URN[] urns = toEncode.getUrns();
    for (int i = 0; i < urns.length; i++){
      try {
        buffer.writeBytes(urns[i].getUrnString().getBytes("UTF-8"));
      } catch (UnsupportedEncodingException e) {
        throw new EncodingException("UTF-8 encoding is not supported by urn: "+urns[i].getUrnString());
      }
      buffer.writeByte((byte) 0x1C);
    }
    if (toEncode.getGGEP() == null){
      buffer.writerIndex(buffer.writerIndex() - 1);
    }
    else{
      ggepEncoder.encode(buffer, new EncoderInput(toEncode.getGGEP(), false));
    }
    buffer.writeByte((byte) 0x0);
  }
}


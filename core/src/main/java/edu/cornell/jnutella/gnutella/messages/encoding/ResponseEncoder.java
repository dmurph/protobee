package edu.cornell.jnutella.gnutella.messages.encoding;

import java.io.UnsupportedEncodingException;

import org.jboss.netty.buffer.ChannelBuffer;

import com.google.inject.Inject;

import edu.cornell.jnutella.gnutella.messages.ResponseBody;
import edu.cornell.jnutella.gnutella.messages.encoding.GGEPEncoder.EncoderInput;
import edu.cornell.jnutella.util.ByteUtils;

public class ResponseEncoder implements PartEncoder<ResponseBody> {
  private final GGEPEncoder ggepEncoder;

  @Inject
  public ResponseEncoder(GGEPEncoder ggepEncoder) {
    this.ggepEncoder = ggepEncoder;
  }

  public void encode(ChannelBuffer buffer, ResponseBody toEncode) throws EncodingException {

    ByteUtils.int2leb((int) toEncode.getFileIndex(), buffer);
    ByteUtils.int2leb((int) toEncode.getFileSize(), buffer);
    try {
      buffer.writeBytes(toEncode.getFileName().getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      throw new EncodingException("UTF-8 not supported by filename "+toEncode.getFileName()+" in response encoder");
    }
    
    buffer.writeByte((byte) 0x0);

    if (toEncode.getURN() != null) {
      try {
        buffer.writeBytes(toEncode.getURN().getAsString().getBytes("UTF-8"));
      } catch (UnsupportedEncodingException e) {
        throw new EncodingException("UTF-8 not supported by filename "+toEncode.getURN().getAsString()+" in response encoder");
      }
      buffer.writeByte((byte) 0x1C);
    }

    if (toEncode.getGgep() != null) {
      EncoderInput ei = new EncoderInput(toEncode.getGgep(), false);
      ggepEncoder.encode(buffer, ei);
    }

    buffer.writeByte((byte) 0x0);
    
    
  }
}
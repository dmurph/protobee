package org.protobee.gnutella.messages.encoding;

import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffer;
import org.protobee.gnutella.messages.ResponseBody;
import org.protobee.gnutella.messages.encoding.GGEPEncoder.EncoderInput;
import org.protobee.util.ByteUtils;

import com.google.inject.Inject;


public class ResponseEncoder implements PartEncoder<ResponseBody> {
  private final GGEPEncoder ggepEncoder;
  private final HUGEEncoder hugeEncoder;

  @Inject
  public ResponseEncoder(GGEPEncoder ggepEncoder, HUGEEncoder hugeEncoder) {
    this.ggepEncoder = ggepEncoder;
    this.hugeEncoder = hugeEncoder;
  }

  public void encode(ChannelBuffer buffer, ResponseBody toEncode) throws EncodingException {

    ByteUtils.int2leb((int) toEncode.getFileIndex(), buffer);
    ByteUtils.int2leb((int) toEncode.getFileSize(), buffer);
    buffer.writeBytes(toEncode.getFileName().getBytes(Charset.forName("UTF-8")));

    buffer.writeByte((byte) 0x0);

    if (toEncode.getHUGE() != null) {
      hugeEncoder.encode(buffer, toEncode.getHUGE());
      buffer.writeByte((byte) 0x1C);
    }

    if (toEncode.getGgep() != null) {
      EncoderInput ei = new EncoderInput(toEncode.getGgep(), false);
      ggepEncoder.encode(buffer, ei);
    }

    buffer.writeByte((byte) 0x0);

  }
}
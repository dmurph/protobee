package org.protobee.gnutella.messages.encoding;

import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffer;
import org.protobee.gnutella.extension.HUGEExtension;
import org.protobee.gnutella.util.URN;

import com.google.inject.Inject;

public class HUGEEncoder implements PartEncoder<HUGEExtension> {

  @Inject
  public HUGEEncoder( ) {
  }

  @Override
  public void encode(ChannelBuffer buffer, HUGEExtension toEncode) throws EncodingException {
    
    URN[] urns = toEncode.getUrns();
    
    for (int i = 0; i < (urns.length - 1); i++) {
      buffer.writeBytes(urns[i].getUrnString().getBytes(Charset.forName("UTF-8")));
      buffer.writeByte((byte) 0x1C);
    }
    buffer.writeBytes(urns[urns.length - 1].getUrnString().getBytes(Charset.forName("UTF-8")));
    
  }
}


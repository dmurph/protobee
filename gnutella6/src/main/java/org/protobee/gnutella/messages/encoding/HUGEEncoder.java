package org.protobee.gnutella.messages.encoding;

import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffer;
import org.protobee.gnutella.extension.HUGEExtension;
import org.protobee.gnutella.util.URN;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class HUGEEncoder implements PartEncoder<HUGEExtension> {

  @Inject
  public HUGEEncoder( ) {
  }

  @Override
  public void encode(ChannelBuffer buffer, HUGEExtension toEncode) throws EncodingException {
    
    Preconditions.checkArgument(toEncode != null, "encode shouldn't be called if HUGE is null");
    
    URN[] urns = toEncode.getUrns();
    int count = 0;
    
    for (URN urn : urns){
      buffer.writeBytes(urn.getUrnString().getBytes(Charset.forName("UTF-8")));
      if (count != urns.length){
        buffer.writeByte((byte) 0x1C);
      }
      count++;
    }
  }
}


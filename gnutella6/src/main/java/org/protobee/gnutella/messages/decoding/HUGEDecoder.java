package org.protobee.gnutella.messages.decoding;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.protobee.gnutella.extension.HUGEExtension;
import org.protobee.gnutella.util.URN;

import com.google.inject.Inject;

public class HUGEDecoder implements PartDecoder<HUGEExtension> {

  @Inject
  public HUGEDecoder() { }

  @Override
  public HUGEExtension decode(ChannelBuffer buffer) throws DecodingException {
    List<URN> urns = new ArrayList<URN>();
    
    if (buffer.readableBytes() < 4){ 
      return new HUGEExtension(urns.toArray(new URN[urns.size()]));
    }
    
    String testForUrn = buffer.toString(buffer.readerIndex(), 4, Charset.forName("UTF-8"));
    
    while(testForUrn.equals("urn:")){
      int lengthURN = buffer.bytesBefore((byte) 0x1C);
      boolean isLast = (lengthURN == -1);

      if (isLast){
        int temp = buffer.bytesBefore((byte) 0x0);
        lengthURN = (temp == -1) ? buffer.readableBytes() : temp;
      }
      
      urns.add(new URN(buffer.toString(buffer.readerIndex(), lengthURN, Charset.forName("UTF-8"))));

      if (!isLast){
        buffer.readerIndex(buffer.readerIndex() + 1);
      }

      buffer.readerIndex(buffer.readerIndex() + lengthURN);

      // update tester string if possible. else set to empty to break loop
      testForUrn = (buffer.readableBytes() > 3) ? buffer.toString(buffer.readerIndex(), 4, Charset.forName("UTF-8")) : "";
    }
    return new HUGEExtension(urns.toArray(new URN[urns.size()]));
  }

}
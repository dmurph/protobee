package org.protobee.gnutella.messages.decoding;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.protobee.gnutella.extension.GGEP;
import org.protobee.gnutella.extension.HUGEExtension;
import org.protobee.gnutella.util.URN;

import com.google.inject.Inject;


public class HUGEDecoder implements PartDecoder<HUGEExtension> {
  private final GGEPDecoder ggepDecoder;

  @Inject
  public HUGEDecoder(GGEPDecoder ggepDecoder) {
    this.ggepDecoder = ggepDecoder;
  }

  @Override
  public HUGEExtension decode(ChannelBuffer buffer) throws DecodingException {

    GGEP ggep = null;
    List<URN> urns = new ArrayList<URN>();
    String testForUrn = buffer.toString(buffer.readerIndex(), buffer.readerIndex()+4, Charset.forName("UTF-8"));
    
    while(testForUrn.equals("urn:")){
      int lengthURN = buffer.bytesBefore((byte) 0x1C);
      if (lengthURN == -1){
        lengthURN = buffer.bytesBefore((byte) 0x0);
        urns.add(new URN(buffer.toString(buffer.readerIndex(), lengthURN, Charset.forName("UTF-8"))));
        return new HUGEExtension(urns.toArray(new URN[urns.size()]), null);
      }
      urns.add(new URN(buffer.toString(buffer.readerIndex(), lengthURN, Charset.forName("UTF-8"))));
      buffer.readerIndex(buffer.readerIndex() + lengthURN + 1);
      if (buffer.readableBytes() > 3){
        testForUrn = buffer.toString(buffer.readerIndex(), 4, Charset.forName("UTF-8"));
      }
      else{
        break;
      }
    }
    if (buffer.readable()){
      ggep = ggepDecoder.decode(buffer);
    }

    return new HUGEExtension(urns.toArray(new URN[urns.size()]), ggep);
  }

}

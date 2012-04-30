package org.protobee.gnutella.messages.decoding;

import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffer;
import org.protobee.annotation.InjectLogger;
import org.protobee.gnutella.extension.GGEP;
import org.protobee.gnutella.messages.ResponseBody;
import org.protobee.gnutella.util.URN;
import org.protobee.gnutella.util.URN.Type;
import org.protobee.util.ByteUtils;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;


public class ResponseDecoder implements PartDecoder<ResponseBody> {
  private final GGEPDecoder ggepDecoder;

  @InjectLogger
  private Logger log;
  
  @Inject
  public ResponseDecoder(GGEPDecoder ggepDecoder) {
    this.ggepDecoder = ggepDecoder;
  }

  @Override
  public ResponseBody decode(ChannelBuffer buffer) throws DecodingException {
    Preconditions.checkState(buffer.readableBytes() >= 8);
    // sets writer index for later use
    buffer.writerIndex(buffer.readableBytes());
    long fileIndex = ByteUtils.uint2long(ByteUtils.leb2int(buffer));
    long fileSize = ByteUtils.uint2long(ByteUtils.leb2int(buffer));
    
    int lengthFileName = buffer.bytesBefore((byte) 0x0);
    if (lengthFileName == -1){
      log.error("Reached end of buffer with no 0 byte in filename");
      throw new DecodingException("Reached end of buffer with no 0 byte in filename");
    }
    String fileName = buffer.toString(buffer.readerIndex(), lengthFileName, Charset.forName("UTF-8"));
    
    buffer.readerIndex(buffer.readerIndex()+lengthFileName);
    
    byte tag = buffer.readByte(); 
    byte check = buffer.readByte();
    
    URN urn = null;
    GGEP ggep = null;
    
    if (tag == ((byte) 0x0) && check == ((byte) 0x0)){
      return new ResponseBody(fileIndex, fileSize, fileName, null, null);
    }
    
    buffer.readerIndex(buffer.readerIndex() - 1);
    
    // will be urn or ggep
    if (tag != ((byte) 0xC3)){
      Type type = Type.ANY_TYPE;
      int lengthAccompaniedURN = buffer.bytesBefore((byte) 0x1C);
      int lengthSoloURN = buffer.bytesBefore((byte) 0x0);
      // if it's a solo urn
      if (lengthAccompaniedURN == -1){
        String urnString = buffer.toString(buffer.readerIndex(), lengthSoloURN, Charset.forName("UTF-8"));
        urn = new URN(urnString);
        return new ResponseBody(fileIndex, fileSize, fileName, urn, null);
      }
      // if not a solo urn
      urn = new URN(buffer.toString(buffer.readerIndex(), lengthAccompaniedURN, Charset.forName("UTF-8")), type);
      buffer.readerIndex(buffer.readerIndex() + lengthAccompaniedURN + 1);
    }
    
    if(buffer.readByte() == ((byte) 0x0)){
      return new ResponseBody(fileIndex, fileSize, fileName, urn, null);
    }

    buffer.readerIndex(buffer.readerIndex()-1);
    ggep = ggepDecoder.decode(buffer);
    buffer.readByte(); // to skip past null
    return new ResponseBody(fileIndex, fileSize, fileName, urn, ggep);
  }
}
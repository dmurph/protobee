package org.protobee.gnutella.messages.decoding;

import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffer;
import org.protobee.annotation.InjectLogger;
import org.protobee.gnutella.extension.GGEP;
import org.protobee.gnutella.extension.HUGEExtension;
import org.protobee.gnutella.messages.ResponseBody;
import org.protobee.util.ByteUtils;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;


public class ResponseDecoder implements PartDecoder<ResponseBody> {
  private final GGEPDecoder ggepDecoder;
  private final HUGEDecoder hugeDecoder;

  @InjectLogger
  private Logger log;

  @Inject
  public ResponseDecoder(GGEPDecoder ggepDecoder, HUGEDecoder hugeDecoder) {
    this.ggepDecoder = ggepDecoder;
    this.hugeDecoder = hugeDecoder;
  }

  @Override
  public ResponseBody decode(ChannelBuffer buffer) throws DecodingException {
    Preconditions.checkState(buffer.readableBytes() >= 8);

    long fileIndex = ByteUtils.uint2long(ByteUtils.leb2int(buffer));
    long fileSize = ByteUtils.uint2long(ByteUtils.leb2int(buffer));

    int lengthFileName = buffer.bytesBefore((byte) 0x0);
    if (lengthFileName == -1){
      log.error("Reached end of buffer with no 0 byte in filename");
      throw new DecodingException("Reached end of buffer with no 0 byte in filename");
    }
    String fileName = buffer.toString(buffer.readerIndex(), lengthFileName, Charset.forName("UTF-8"));
    buffer.readerIndex(buffer.readerIndex()+lengthFileName+1);

    int lengthOfExtensions = buffer.bytesBefore((byte) 0x0);
    if (lengthOfExtensions == -1){
      log.error("Reached end of buffer with no 0 byte ending the extension block");
      throw new DecodingException("Reached end of buffer with no 0 byte ending the extension block");
    }

    HUGEExtension huge = null;
    GGEP ggep = null;

    if (lengthOfExtensions != 0){

      byte tag = buffer.readByte(); 
      buffer.readerIndex(buffer.readerIndex() - 1);

      if (tag != (byte) 0xC3){ // if not ggep
        huge = hugeDecoder.decode(buffer);
        buffer.readByte(); // read the dividor bite or the ending bite
        if (buffer.readable()){
          tag = buffer.readByte();
          buffer.readerIndex(buffer.readerIndex() - 1);
        }
      }

      if (tag == (byte) 0xC3){
        ggep = ggepDecoder.decode(buffer);
      }

    }
    buffer.readByte();
    return new ResponseBody(fileIndex, fileSize, fileName, huge, ggep);
  }
}
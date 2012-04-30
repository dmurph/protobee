package org.protobee.gnutella.messages.decoding;

import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffer;
import org.protobee.gnutella.extension.GGEP;
import org.protobee.gnutella.extension.HUGEExtension;
import org.protobee.gnutella.messages.MessageBodyFactory;
import org.protobee.gnutella.messages.MessageHeader;
import org.protobee.gnutella.messages.QueryBody;
import org.protobee.gnutella.session.ForMessageType;
import org.protobee.util.ByteUtils;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;


@ForMessageType(MessageHeader.F_QUERY)
public class QueryDecoder implements MessageBodyDecoder<QueryBody> {
  private final MessageBodyFactory bodyFactory;
  private final GGEPDecoder ggepDecoder;
  private final HUGEDecoder hugeDecoder;
  
  @Inject
  public QueryDecoder(MessageBodyFactory bodyFactory, GGEPDecoder ggepDecoder, HUGEDecoder hugeDecoder) {
    this.bodyFactory = bodyFactory;
    this.ggepDecoder = ggepDecoder;
    this.hugeDecoder = hugeDecoder;
  }

  // TODO check query length
  // if (query.length() > MAX_QUERY_LENGTH){ }
  
  @Override
  public QueryBody decode(ChannelBuffer buffer) throws DecodingException {
    Preconditions.checkState(buffer.readableBytes() >= 3);
    
    short minSpeed = (ByteUtils.leb2short(buffer));
    int lengthQuery = buffer.bytesBefore((byte) 0x0);
    String query = buffer.toString(buffer.readerIndex(), lengthQuery, Charset.forName("UTF-8"));
    buffer.readerIndex(buffer.readerIndex()+lengthQuery+1);
    
    if (!buffer.readable()) {
      return bodyFactory.createQueryMessage(minSpeed, query, null, null);
    }

    byte tag = buffer.readByte();
    buffer.readerIndex(buffer.readerIndex() - 1);
    
    HUGEExtension huge = null; 
    if (tag != ((byte) 0xC3)){
      huge = hugeDecoder.decode(buffer);
      buffer.readByte();
    }
    
    GGEP ggep = null;
    
    if (buffer.readable()) {
      ggep = ggepDecoder.decode(buffer);
    }
    
    Preconditions.checkNotNull(ggep, "GGEP is null.");
    return bodyFactory.createQueryMessage(minSpeed, query, huge, ggep);
  }
}
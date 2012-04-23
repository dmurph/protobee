package edu.cornell.jnutella.gnutella.messages.decoding;

import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.extension.GGEP;
import edu.cornell.jnutella.extension.HUGEExtension;
import edu.cornell.jnutella.gnutella.messages.MessageBodyFactory;
import edu.cornell.jnutella.gnutella.messages.MessageHeader;
import edu.cornell.jnutella.gnutella.messages.QueryBody;
import edu.cornell.jnutella.gnutella.session.ForMessageType;
import edu.cornell.jnutella.util.ByteUtils;

@ForMessageType(MessageHeader.F_QUERY)
public class QueryDecoder implements MessageBodyDecoder<QueryBody> {
  private final MessageBodyFactory bodyFactory;
  private final GGEPDecoder ggepDecoder;
  private final HUGEDecoder hugeDecoder;

  @InjectLogger
  private Logger log;
  
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
package edu.cornell.jnutella.messages.decoding;

import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.extension.GGEP;
import edu.cornell.jnutella.messages.MessageBodyFactory;
import edu.cornell.jnutella.messages.MessageHeader;
import edu.cornell.jnutella.messages.QueryBody;
import edu.cornell.jnutella.session.gnutella.ForMessageType;
import edu.cornell.jnutella.util.ByteUtils;

@ForMessageType(MessageHeader.F_QUERY)
public class QueryDecoder implements MessageBodyDecoder<QueryBody> {
  private final MessageBodyFactory bodyFactory;
  private final GGEPDecoder ggepDecoder;

  @InjectLogger
  private Logger log;
  
  @Inject
  public QueryDecoder(MessageBodyFactory bodyFactory, GGEPDecoder ggepDecoder) {
    this.bodyFactory = bodyFactory;
    this.ggepDecoder = ggepDecoder;
  }

  @Override
  public QueryBody decode(ChannelBuffer buffer) throws DecodingException {
    Preconditions.checkState(buffer.readable());
    short minSpeed = (ByteUtils.leb2short(buffer));
    int startIndex = buffer.readerIndex();
    byte currByte = 0x1;
    while (buffer.readable()){
      currByte = buffer.readByte();
      if (currByte == 0x0){
        break;
      }
    }
    
    if(currByte != 0x0) {
      log.error("Reached end of buffer with no 0 byte in query");
      throw new DecodingException("Reached end of buffer with no 0 byte in query");
    }
    
    int endIndex = buffer.readerIndex();
    String query = buffer.toString(startIndex, endIndex-startIndex-1, Charset.forName("UTF-8"));
    
    // TODO check query length
    // if (query.length() > MAX_QUERY_LENGTH){ }
    
    if (!buffer.readable()) {
      return bodyFactory.createQueryMessage(minSpeed, query, null);
    }

    GGEP ggep = ggepDecoder.decode(buffer);
    Preconditions.checkNotNull(ggep, "GGEP is null.");
    return bodyFactory.createQueryMessage(minSpeed, query, ggep);
  }
}
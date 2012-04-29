package edu.cornell.jnutella.gnutella.routing.message;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.gnutella.messages.MessageBodyFactory;
import edu.cornell.jnutella.gnutella.messages.MessageHeader;
import edu.cornell.jnutella.gnutella.messages.decoding.DecodingException;
import edu.cornell.jnutella.gnutella.messages.decoding.MessageBodyDecoder;
import edu.cornell.jnutella.gnutella.session.ForMessageType;
import edu.cornell.jnutella.util.ByteUtils;

@ForMessageType(MessageHeader.F_ROUTE_TABLE_UPDATE)
public class RoutingDecoder implements MessageBodyDecoder<RoutingBody> {
  private final MessageBodyFactory bodyFactory;

  @InjectLogger
  private Logger log;

  @Inject
  public RoutingDecoder(MessageBodyFactory bodyFactory) {
    this.bodyFactory = bodyFactory;
  }

  @Override
  public RoutingBody decode(ChannelBuffer buffer) throws DecodingException {

    byte variant = buffer.readByte();

    if (variant == RoutingBody.RESET_TABLE_VARIANT){
      Preconditions.checkArgument(buffer.readableBytes() == 5);
      long tableLength = ByteUtils.uint2long(ByteUtils.leb2int(buffer));
      byte infinity = buffer.readByte();
      return bodyFactory.createResetMessage(tableLength, infinity);
    }
    else if (variant == RoutingBody.PATCH_TABLE_VARIANT){
      Preconditions.checkArgument(buffer.readableBytes() >= 4);
      byte sequenceNum = buffer.readByte();
      byte sequenceSize = buffer.readByte();
      byte compressor = buffer.readByte();
      byte entryBits = buffer.readByte();
      List<Byte> d = new ArrayList<Byte>();
      while (buffer.readable()){
        d.add(buffer.readByte());
      }
      byte[] data = new byte[d.size()];
      for (int i = 0; i < d.size(); i++){
        data[i] = (byte) d.get(i);
      }
      return bodyFactory.createPatchMessage(sequenceNum, sequenceSize, compressor, entryBits, data);
    }
    else{
      log.error("Not a ResetBody or a PatchBody.");
      throw new DecodingException("Not a ResetBody or a PatchBody.");
    }
  }
}

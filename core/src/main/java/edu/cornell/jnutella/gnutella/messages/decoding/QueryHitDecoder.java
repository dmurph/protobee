package edu.cornell.jnutella.gnutella.messages.decoding;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import org.jboss.netty.buffer.ChannelBuffer;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import edu.cornell.jnutella.extension.GGEP;
import edu.cornell.jnutella.gnutella.messages.EQHDBody;
import edu.cornell.jnutella.gnutella.messages.MessageBodyFactory;
import edu.cornell.jnutella.gnutella.messages.MessageHeader;
import edu.cornell.jnutella.gnutella.messages.QueryHitBody;
import edu.cornell.jnutella.gnutella.messages.ResponseBody;
import edu.cornell.jnutella.gnutella.session.ForMessageType;
import edu.cornell.jnutella.util.ByteUtils;
import edu.cornell.jnutella.util.GUID;
import edu.cornell.jnutella.util.HexConverter;

@ForMessageType(MessageHeader.F_QUERY_REPLY)
public class QueryHitDecoder implements MessageBodyDecoder<QueryHitBody> {
  private final MessageBodyFactory bodyFactory;
  private final GGEPDecoder ggepDecoder;
  private final ResponseDecoder responseDecoder;
  private final EQHDDecoder eqhdDecoder;

  @Inject
  public QueryHitDecoder(MessageBodyFactory bodyFactory, GGEPDecoder ggepDecoder, ResponseDecoder responseDecoder, EQHDDecoder eqhdDecoder) {
    this.bodyFactory = bodyFactory;
    this.ggepDecoder = ggepDecoder;
    this.responseDecoder = responseDecoder; 
    this.eqhdDecoder = eqhdDecoder; 
  }

  @Override
  public QueryHitBody decode(ChannelBuffer buffer) throws DecodingException {
    Preconditions.checkState(buffer.readableBytes() >= 11);

    byte numHits = buffer.readByte();
    int port = ByteUtils.ushort2int(ByteUtils.leb2short(buffer));
    byte[] address = {buffer.readByte(), buffer.readByte(), buffer.readByte(), buffer.readByte()};
    InetSocketAddress socketAddress = ByteUtils.getInetSocketAddress(address, port);
    long speed = ByteUtils.uint2long(ByteUtils.leb2int(buffer));

    ArrayList<ResponseBody> hitList = new ArrayList<ResponseBody>();
    for (int i = 0; i < numHits; i++){
      hitList.add(responseDecoder.decode(buffer));
    }

    EQHDBody eqhd = eqhdDecoder.decode(buffer);

    Preconditions.checkArgument(buffer.readableBytes() >= 16);

    byte[] privateArea1 = null;
    GGEP ggep = null;
    byte[] privateArea2 = null;

    int startIndexPrivateData = buffer.readerIndex();
    int endIndexPrivateData = buffer.readerIndex() + buffer.readableBytes() - 16;

    if (startIndexPrivateData != endIndexPrivateData){
      // look for private area
      int privateArea1Length = buffer.bytesBefore((endIndexPrivateData - startIndexPrivateData), (byte) 0xC3);
      boolean noGGEP = (privateArea1Length == -1);
      privateArea1 = (noGGEP) ? new byte[endIndexPrivateData - startIndexPrivateData] : new byte[privateArea1Length];
      for (int i = 0; i < privateArea1.length; i++){
        privateArea1[i] = buffer.readByte();
      }
      ggep = (noGGEP) ? null : ggepDecoder.decode(buffer);
      privateArea2 = (buffer.readerIndex() == endIndexPrivateData) ? new byte[0]: new byte[endIndexPrivateData - buffer.readerIndex()];
      for (int i = 0; i < privateArea2.length; i++){
        privateArea2[i] = buffer.readByte();
      }
    }
    
    Preconditions.checkArgument(buffer.readableBytes() == 16);
    
    byte[] servantID = new byte[16];
    buffer.readBytes(servantID);
    
    return bodyFactory.createQueryHitMessage( socketAddress, speed, hitList.toArray(new ResponseBody[hitList.size()]), 
      eqhd.getVendorCode(), eqhd.getFlags(), eqhd.getControls(),
      privateArea1, ggep, new byte[4], privateArea2, servantID);

  }
}
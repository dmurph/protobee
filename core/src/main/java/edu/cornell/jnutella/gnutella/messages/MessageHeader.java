package edu.cornell.jnutella.gnutella.messages;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import edu.cornell.jnutella.gnutella.routing.InvalidMessageException;
import edu.cornell.jnutella.util.GUID;


public class MessageHeader {

  public static interface Factory {
    MessageHeader create(byte[] guid, @Assisted("payloadType") byte payloadType,
        @Assisted("ttl") byte ttl, @Assisted("hops") byte hops, int payloadLength);

    MessageHeader create(@Assisted byte[] guid, @Assisted("payloadType") byte payloadType,
        @Assisted("ttl") byte ttl, @Assisted("hops") byte hops);
  }

  public static final int UNKNOWN_PAYLOAD_LENGTH = -1;

  public static final int MESSAGE_HEADER_LENGTH = 23;

  public static final byte F_PING = (byte) 0x0;

  public static final byte F_PING_REPLY = (byte) 0x1;

  public static final byte F_PUSH = (byte) 0x40;

  public static final byte F_QUERY = (byte) 0x80;

  public static final byte F_QUERY_REPLY = (byte) 0x81;

  public static final byte F_ROUTE_TABLE_UPDATE = (byte) 0x30;

  public static final byte F_VENDOR_MESSAGE = (byte) 0x31;

  public static final byte F_VENDOR_MESSAGE_STABLE = (byte) 0x32;

  public static final byte F_UDP_CONNECTION = (byte) 0x41;


  private final byte[] guid;
  private final byte payloadType;
  private final byte ttl;
  private final byte hops;
  private int payloadLength;

  @AssistedInject
  public MessageHeader(@Assisted byte[] guid, @Assisted("payloadType") byte payloadType,
      @Assisted("ttl") byte ttl, @Assisted("hops") byte hops, @Assisted int payloadLength) {
    this.guid = guid;
    this.payloadType = payloadType;
    this.ttl = ttl;
    this.hops = hops;
    this.payloadLength = payloadLength;
  }

  @AssistedInject
  public MessageHeader(@Assisted byte[] guid, @Assisted("payloadType") byte payloadType,
      @Assisted("ttl") byte ttl, @Assisted("hops") byte hops) {
    this.guid = guid;
    this.payloadType = payloadType;
    this.ttl = ttl;
    this.hops = hops;
    this.payloadLength = UNKNOWN_PAYLOAD_LENGTH;
  }

  public byte getTtl() {
    return ttl;
  }

  public byte getHops() {
    return hops;
  }

  public int getPayloadLength() {
    return payloadLength;
  }
  
  public void setPayloadLength(int payloadLength) {
    this.payloadLength = payloadLength;
  }

  public boolean isPayloadLengthUnknown() {
    return payloadLength == UNKNOWN_PAYLOAD_LENGTH;
  }

  public byte[] getGuid() {
    return guid;
  }
  
  public GUID getGUID() throws InvalidMessageException{
    return new GUID(guid);
  }

  public byte getPayloadType() {
    return payloadType;
  }
}

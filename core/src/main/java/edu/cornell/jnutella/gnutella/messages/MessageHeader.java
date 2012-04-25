package edu.cornell.jnutella.gnutella.messages;

import java.util.Arrays;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;


public class MessageHeader {

  public static interface Factory {
    MessageHeader create(byte[] guid, @Assisted("payloadType") byte payloadType,
        @Assisted("ttl") byte ttl, @Assisted("hops") byte hops, int payloadLength);
    

    MessageHeader create(byte[] guid, @Assisted("payloadType") byte payloadType,
        @Assisted("ttl") byte ttl);

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
  
  @AssistedInject
  public MessageHeader(@Assisted byte[] guid, @Assisted("payloadType") byte payloadType,
      @Assisted("ttl") byte ttl) {
    this.guid = guid;
    this.payloadType = payloadType;
    this.ttl = ttl;
    this.hops = 0;
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

  public byte getPayloadType() {
    return payloadType;
  }

  @Override
  public String toString() {
    return "{ guid: " + guid + ", hops: " + hops + ", ttl: " + ttl + ", payloadType: "
        + payloadType + ", payloadLength: " + payloadLength + "}";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(guid);
    result = prime * result + hops;
    result = prime * result + payloadLength;
    result = prime * result + payloadType;
    result = prime * result + ttl;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    MessageHeader other = (MessageHeader) obj;
    if (!Arrays.equals(guid, other.guid)) return false;
    if (hops != other.hops) return false;
    if (payloadLength != other.payloadLength) return false;
    if (payloadType != other.payloadType) return false;
    if (ttl != other.ttl) return false;
    return true;
  }
}

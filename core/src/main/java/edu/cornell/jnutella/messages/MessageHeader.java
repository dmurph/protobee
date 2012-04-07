package edu.cornell.jnutella.messages;


public class MessageHeader {
  
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
  private byte ttl;
  private byte hops;
  private int payloadLength;

  public MessageHeader(byte[] guid, byte payloadType, byte ttl, byte hops, int payloadLength) {
    this.guid = guid;
    this.payloadType = payloadType;
    this.ttl = ttl;
    this.hops = hops;
    this.payloadLength = payloadLength;
  }

  public byte getTtl() {
    return ttl;
  }

  public void setTtl(byte ttl) {
    this.ttl = ttl;
  }

  public byte getHops() {
    return hops;
  }

  public void setHops(byte hops) {
    this.hops = hops;
  }

  public int getPayloadLength() {
    return payloadLength;
  }

  public void setPayloadLength(int payloadLength) {
    this.payloadLength = payloadLength;
  }

  public byte[] getGuid() {
    return guid;
  }

  public byte getPayloadType() {
    return payloadType;
  }
}

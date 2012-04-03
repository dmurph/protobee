package edu.cornell.jnutella.messages;

public class GnutellaMessage {

  private final MessageHeader header;
  private final MessageBody body;

  public GnutellaMessage(MessageHeader header, MessageBody body) {
    this.header = header;
    this.body = body;
  }

  public MessageHeader getHeader() {
    return header;
  }

  public MessageBody getBody() {
    return body;
  }
}

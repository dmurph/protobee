package org.protobee.gnutella.messages;

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

  @Override
  public String toString() {
    return "{ header: " + header + ", body: " + body + "}";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((body == null) ? 0 : body.hashCode());
    result = prime * result + ((header == null) ? 0 : header.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    GnutellaMessage other = (GnutellaMessage) obj;
    if (body == null) {
      if (other.body != null) return false;
    } else if (!body.equals(other.body)) return false;
    if (header == null) {
      if (other.header != null) return false;
    } else if (!header.equals(other.header)) return false;
    return true;
  }
}

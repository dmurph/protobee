package org.protobee.examples.questions;

import org.protobee.examples.protos.BroadcasterProtos.Header;

public class QuestionMessage {
  private final Header header;
  private final Object body;

  public QuestionMessage(Header header, Object body) {
    this.header = header;
    this.body = body;
  }

  public Object getBody() {
    return body;
  }

  public Header getHeader() {
    return header;
  }
}

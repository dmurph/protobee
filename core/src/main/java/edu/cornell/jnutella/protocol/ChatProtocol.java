package edu.cornell.jnutella.protocol;

public class ChatProtocol implements Protocol {

  @Override
  public String getName() {
    return "Chat";
  }

  @Override
  public String getVersion() {
    return "0.1";
  }

  @Override
  public String getHeaderRegex() {
    return "^CHAT CONNECT/0\\.1$";
  }
}

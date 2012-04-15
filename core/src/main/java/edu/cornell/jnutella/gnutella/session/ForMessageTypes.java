package edu.cornell.jnutella.gnutella.session;

public class ForMessageTypes {

  private ForMessageTypes() {}

  public static ForMessageType with(byte type) {
    return new ForMessageTypeAnnotation(type);
  }
}

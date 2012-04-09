package edu.cornell.jnutella.session.gnutella;

public class ForMessageTypes {

  private ForMessageTypes() {}

  public static ForMessageType with(byte type) {
    return new ForMessageTypeAnnotation(type);
  }
}

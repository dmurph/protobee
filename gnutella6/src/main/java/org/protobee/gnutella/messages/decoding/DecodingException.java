package org.protobee.gnutella.messages.decoding;

public class DecodingException extends Exception {
  private static final long serialVersionUID = 1L;

  public DecodingException() {
    super();
  }

  public DecodingException(String message, Throwable cause) {
    super(message, cause);
  }

  public DecodingException(String message) {
    super(message);
  }

  public DecodingException(Throwable cause) {
    super(cause);
  }
}

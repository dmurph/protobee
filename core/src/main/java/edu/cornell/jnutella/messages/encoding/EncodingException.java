package edu.cornell.jnutella.messages.encoding;

public class EncodingException extends Exception {
  private static final long serialVersionUID = 1L;

  public EncodingException() {
    super();
  }

  public EncodingException(String message, Throwable cause) {
    super(message, cause);
  }

  public EncodingException(String message) {
    super(message);
  }

  public EncodingException(Throwable cause) {
    super(cause);
  }
}

package edu.cornell.jnutella.gnutella.routing;

/**
 * Thrown when a GGEP block is hopeless corrupt, making it impossible to extract any of the
 * extensions.
 */
public class InvalidMessageException extends Exception {
  private static final long serialVersionUID = 1L;

  public InvalidMessageException() {}

  public InvalidMessageException(String msg) {
    super(msg);
  }

  public InvalidMessageException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public InvalidMessageException(Throwable cause) {
    super(cause);
  }
}

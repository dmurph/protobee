package edu.cornell.jnutella.extension;

/**
 * Thrown when a GGEP extension cannot be found or parsed. Typically other extensions in the block
 * can be extracted.
 */
public class BadGGEPPropertyException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public BadGGEPPropertyException() {}

  public BadGGEPPropertyException(String msg) {
    super(msg);
  }

  public BadGGEPPropertyException(String message, Throwable cause) {
    super(message, cause);
  }

  public BadGGEPPropertyException(Throwable cause) {
    super(cause);
  }
}

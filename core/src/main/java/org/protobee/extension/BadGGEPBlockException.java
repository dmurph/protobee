package org.protobee.extension;

/**
 * Thrown when a GGEP block is hopeless corrupt, making it impossible to extract any of the
 * extensions.
 */
public class BadGGEPBlockException extends Exception {
  private static final long serialVersionUID = 1L;

  public BadGGEPBlockException() {}

  public BadGGEPBlockException(String msg) {
    super(msg);
  }

  public BadGGEPBlockException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public BadGGEPBlockException(Throwable cause) {
    super(cause);
  }
}

package edu.cornell.jnutella.gnutella.messages.encoding;

  public class EncodingException extends Exception {
    private static final long serialVersionUID = 1L;

    public EncodingException() {}

    public EncodingException(String msg) {
      super(msg);
    }

    public EncodingException(String msg, Throwable cause) {
      super(msg, cause);
    }

    public EncodingException(Throwable cause) {
      super(cause);
    }
}

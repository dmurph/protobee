package edu.cornell.jnutella.protocol;

public class GnutellaProtocol implements Protocol {

  @Override
  public String getName() {
    return "Gnutella";
  }

  @Override
  public String getVersion() {
    return "0.6";
  }

  @Override
  public String getHeaderRegex() {
    return "^GNUTELLA CONNECT/0\\.6$";
  }

}

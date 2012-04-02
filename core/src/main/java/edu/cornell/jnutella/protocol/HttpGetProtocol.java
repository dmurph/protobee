package edu.cornell.jnutella.protocol;

public class HttpGetProtocol implements Protocol {

  @Override
  public String getName() {
    return "Http Get";
  }

  @Override
  public String getVersion() {
    return "1.1";
  }

  @Override
  public String getHeaderRegex() {
    return "^GET /[\\d]+/[^\\\\/:\\*\\\"<>\\|]+ HTTP/1.1$";
  }

}

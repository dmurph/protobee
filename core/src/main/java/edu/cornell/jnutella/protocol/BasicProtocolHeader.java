package edu.cornell.jnutella.protocol;

public class BasicProtocolHeader implements ProtocolHeader {
  private final String name, value;
  
  public BasicProtocolHeader(String name, String value) {
    this.name = name;
    this.value = value;
  }
  
  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getValue() {
    return value;
  }
}

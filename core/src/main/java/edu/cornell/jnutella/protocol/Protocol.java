package edu.cornell.jnutella.protocol;

public interface Protocol {

  public String getName();

  public String getVersion();

  public String getHeaderRegex();
}

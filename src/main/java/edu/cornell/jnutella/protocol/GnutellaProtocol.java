package edu.cornell.jnutella.protocol;

/**
 * Protocol interface. Defines the name and version of the protocol, and is also the place to
 * specify visitor methods.
 * 
 * @author Daniel
 */
public interface GnutellaProtocol {

  /**
   * Gets the unique name of the protocol. This is what's written at the start of the header.
   * 
   * @return
   */
  public String getName();

  /**
   * Gets the version of the protocol.
   * 
   * @return
   */
  public String getVersion();
}

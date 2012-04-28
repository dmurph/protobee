package edu.cornell.jnutella.gnutella;

/**
 * TODO: incorporate this into the main framework, so we can reject connections before we handshake
 * 
 * @author Daniel
 */
public interface SlotsController {
  boolean canAcceptNewConnection();
}

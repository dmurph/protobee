package edu.cornell.jnutella.util;

import edu.cornell.jnutella.protocol.Protocol;

public class ProtocolUtils {

  public static final String toString(Protocol protocol) {
    return protocol.name() + "/" + protocol.majorVersion() + "." + protocol.minorVersion();
  }
}

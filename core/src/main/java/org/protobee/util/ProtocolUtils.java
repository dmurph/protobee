package org.protobee.util;

import org.protobee.protocol.Protocol;

public class ProtocolUtils {

  public static final String toString(Protocol protocol) {
    return protocol.name() + "/" + protocol.majorVersion() + "." + protocol.minorVersion();
  }
}

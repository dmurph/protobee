package edu.cornell.jnutella.modules.listeners;

import java.nio.ByteBuffer;

public interface BinaryMessageListener {
  void onBinaryMessage(byte type, ByteBuffer message);
}

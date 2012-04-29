package edu.cornell.jnutella.stats;

import java.net.SocketAddress;

import org.slf4j.Logger;

import com.google.inject.Singleton;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.protocol.Protocol;

@Singleton
public class LoggingDropLog implements DropLog {

  @InjectLogger
  private Logger log;

  @Override
  public void connectionDisconnecting(SocketAddress address, Protocol protocol, String reason) {
    log.info("Connection disconnecting: { address: " + address + ", protocol: " + protocol
        + ", reason: " + reason + "}");
  }

  @Override
  public void connectionDisconnected(SocketAddress address, Protocol protocol) {
    log.info("Connection disconneted: { address: " + address + ", protocol: " + protocol + "}");
  }

  @Override
  public void messageDropped(SocketAddress address, Protocol protocol, Object message, String reason) {
    log.info("Message dropped: { address: " + address + ", protocol: " + protocol + ", message: "
        + message + ", reason: " + reason + "}");
  }
}

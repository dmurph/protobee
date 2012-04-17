package edu.cornell.jnutella.protocol.session;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public interface HandshakeInterruptor {
  void clear();
  void disconnectWithStatus(HttpResponseStatus status);
  void cancelDisconnectAttemptForStatus(HttpResponseStatus status);
  HttpResponseStatus getInterruptingDisconnect();
}

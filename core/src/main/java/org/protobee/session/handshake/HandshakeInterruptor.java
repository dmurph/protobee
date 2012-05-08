package org.protobee.session.handshake;

import java.util.Set;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public interface HandshakeInterruptor {
  void clear();
  void disconnectWithStatus(HttpResponseStatus status);
  void cancelDisconnectAttemptForStatus(HttpResponseStatus status);
  Set<HttpResponseStatus> getDisconnectionInterrupts();
}

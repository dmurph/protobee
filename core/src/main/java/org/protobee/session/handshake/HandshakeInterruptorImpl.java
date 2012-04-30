package org.protobee.session.handshake;

import java.util.Set;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.google.common.collect.Sets;

public class HandshakeInterruptorImpl implements HandshakeInterruptor {

  private final Set<HttpResponseStatus> disconnects = Sets.newHashSet();
  
  @Override
  public void clear() {
    disconnects.clear();
  }
  
  @Override
  public void disconnectWithStatus(HttpResponseStatus status) {
    disconnects.add(status);
  }

  @Override
  public void cancelDisconnectAttemptForStatus(HttpResponseStatus status) {
    disconnects.remove(status);
  }

  @Override
  public HttpResponseStatus getInterruptingDisconnect() {
    return disconnects.iterator().next();
  }
}

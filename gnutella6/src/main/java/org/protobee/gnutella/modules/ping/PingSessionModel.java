package org.protobee.gnutella.modules.ping;

import java.util.concurrent.atomic.AtomicInteger;

import org.protobee.gnutella.constants.MaxTTL;
import org.protobee.guice.scopes.SessionScope;

import com.google.inject.Inject;


@SessionScope
public class PingSessionModel {

  private long acceptTime;
  private byte[] acceptGuid;
  private AtomicInteger[] needed;

  @Inject
  public PingSessionModel(@MaxTTL int maxTtl) {
    acceptTime = 0;
    acceptGuid = null;
    needed = new AtomicInteger[maxTtl + 1];
    for (int i = 0; i < needed.length; i++) {
      needed[i] = new AtomicInteger(0);
    }
  }

  public long getAcceptTime() {
    return acceptTime;
  }

  public void setAcceptTime(long acceptTime) {
    this.acceptTime = acceptTime;
  }

  public byte[] getAcceptGuid() {
    return acceptGuid;
  }

  public void setAcceptGuid(byte[] acceptGuid) {
    this.acceptGuid = acceptGuid;
  }

  public AtomicInteger[] getNeeded() {
    return needed;
  }
}

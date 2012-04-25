package edu.cornell.jnutella.gnutella.modules.ping;

import java.util.concurrent.atomic.AtomicInteger;

import com.google.inject.Inject;

import edu.cornell.jnutella.gnutella.modules.MaxTTL;
import edu.cornell.jnutella.guice.SessionScope;

@SessionScope
public class PingSessionModel {

  private long acceptTime;
  private byte[] acceptGuid;
  private AtomicInteger[] needed;

  @Inject
  public PingSessionModel(@MaxTTL int maxTtl) {
    acceptTime = 0;
    acceptGuid = null;
    needed = new AtomicInteger[maxTtl];
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

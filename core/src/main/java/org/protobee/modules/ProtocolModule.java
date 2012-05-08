package org.protobee.modules;

import org.protobee.compatability.Headers;

import com.google.common.base.Preconditions;


/**
 * Protocol module
 * 
 * @author Daniel
 * 
 */
public abstract class ProtocolModule {

  private final Headers headers;

  public ProtocolModule() {
    this.headers = this.getClass().getAnnotation(Headers.class);
    Preconditions.checkNotNull(headers, "ProtocolModule must be annotated by @Headers");
  }

  public Headers getHeaders() {
    return headers;
  }
}

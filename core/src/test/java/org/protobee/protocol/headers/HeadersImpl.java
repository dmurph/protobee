package org.protobee.protocol.headers;

import java.lang.annotation.Annotation;
import java.util.Arrays;

import org.protobee.protocol.headers.CompatabilityHeader;
import org.protobee.protocol.headers.Headers;

public class HeadersImpl implements Headers {

  private final CompatabilityHeader[] required;
  private final CompatabilityHeader[] requested;

  public HeadersImpl(CompatabilityHeader[] required, CompatabilityHeader[] requested) {
    this.required = required;
    this.requested = requested;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return Headers.class;
  }

  @Override
  public CompatabilityHeader[] required() {
    return required;
  }

  @Override
  public CompatabilityHeader[] requested() {
    return requested;
  }

  @Override
  public int hashCode() {
    return (127 * "required".hashCode()) ^ Arrays.hashCode(required)
        + (127 * "requested".hashCode()) ^ Arrays.hashCode(requested);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    HeadersImpl other = (HeadersImpl) obj;
    if (!Arrays.equals(requested, other.requested)) return false;
    if (!Arrays.equals(required, other.required)) return false;
    return true;
  }
}

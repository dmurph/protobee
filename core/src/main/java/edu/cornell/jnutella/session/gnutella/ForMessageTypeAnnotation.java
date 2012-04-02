package edu.cornell.jnutella.session.gnutella;

import java.io.Serializable;
import java.lang.annotation.Annotation;


class ForMessageTypeAnnotation implements ForMessageType, Serializable {
  private static final long serialVersionUID = 1L;

  private final byte value;

  public ForMessageTypeAnnotation(byte value) {
    this.value = value;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return ForMessageType.class;
  }

  @Override
  public byte value() {
    return value;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ForMessageType)) {
      return false;
    }
    ForMessageType other = (ForMessageType) obj;
    return value == other.value();
  }

  @Override
  public int hashCode() {
    return (127 * "value".hashCode()) ^ Byte.valueOf(value).hashCode();
  }

  public String toString() {
    return "@" + ForMessageType.class.getName() + "(value=" + value + ")";
  }
}

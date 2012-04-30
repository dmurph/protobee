package org.protobee.protocol.headers;

import java.lang.annotation.Annotation;

import org.protobee.protocol.headers.CompatabilityHeader;

public class CompatabilityHeaderImpl implements CompatabilityHeader {

  private final String name;
  private final String minVersion;
  private final String maxVersion;

  public CompatabilityHeaderImpl(String name, String minVersion, String maxVersion) {
    this.name = name;
    this.minVersion = minVersion;
    this.maxVersion = maxVersion;
  }

  @Override
  public String name() {
    return name;
  }


  @Override
  public String minVersion() {
    return minVersion;
  }


  @Override
  public String maxVersion() {
    return maxVersion;
  }


  @Override
  public Class<? extends Annotation> annotationType() {
    return CompatabilityHeader.class;
  }

  @Override
  public int hashCode() {
    return (127 * "name".hashCode()) ^ name.hashCode() + (127 * "minVersion".hashCode())
        ^ minVersion.hashCode() + (127 * "maxVersion".hashCode()) ^ maxVersion.hashCode();
  }


  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    CompatabilityHeaderImpl other = (CompatabilityHeaderImpl) obj;
    if (maxVersion == null) {
      if (other.maxVersion != null) return false;
    } else if (!maxVersion.equals(other.maxVersion)) return false;
    if (minVersion == null) {
      if (other.minVersion != null) return false;
    } else if (!minVersion.equals(other.minVersion)) return false;
    if (name == null) {
      if (other.name != null) return false;
    } else if (!name.equals(other.name)) return false;
    return true;
  }
}
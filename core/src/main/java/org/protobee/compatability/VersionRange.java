package org.protobee.compatability;

import java.util.Comparator;

import org.protobee.protocol.headers.CompatabilityHeader;
import org.protobee.util.VersionComparator;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

public class VersionRange {
  private final String minVersion;
  private final String maxVersion;

  public VersionRange(String minVersion, String maxVersion) {
    Preconditions.checkNotNull(minVersion);
    Preconditions.checkNotNull(maxVersion);
    this.minVersion = minVersion;
    this.maxVersion = maxVersion;
  }

  public VersionRange(CompatabilityHeader header) {
    this(header.minVersion(), header.maxVersion());
  }

  public VersionRange(VersionRange other) {
    this(other.getMinVersion(), other.getMaxVersion());
  }

  public String getMinVersion() {
    return minVersion;
  }

  public String getMaxVersion() {
    return maxVersion;
  }

  @Override
  public String toString() {
    return "{ minVersion:" + minVersion + ", maxVersion:" + maxVersion + "}";
  }


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((maxVersion == null) ? 0 : maxVersion.hashCode());
    result = prime * result + ((minVersion == null) ? 0 : minVersion.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    VersionRange other = (VersionRange) obj;
    if (maxVersion == null) {
      if (other.maxVersion != null) return false;
    } else if (!maxVersion.equals(other.maxVersion)) return false;
    if (minVersion == null) {
      if (other.minVersion != null) return false;
    } else if (!minVersion.equals(other.minVersion)) return false;
    return true;
  }

  public static BUILDER builder() {
    return new BUILDER();
  }

  public static class BUILDER {
    private String minVersion;
    private String maxVersion;

    public BUILDER set(VersionRange entry) {
      minVersion = entry.minVersion;
      maxVersion = entry.maxVersion;
      return this;
    }

    public String getMinVersion() {
      return minVersion;
    }

    public BUILDER setMinVersion(String minVersion) {
      this.minVersion = minVersion;
      return this;
    }

    public String getMaxVersion() {
      return maxVersion;
    }

    public BUILDER setMaxVersion(String maxVersion) {
      this.maxVersion = maxVersion;
      return this;
    }

    public VersionRange build() {
      return new VersionRange(minVersion, maxVersion);
    }
  }

  @Singleton
  public static class MinVersionComparator implements Comparator<VersionRange> {

    private final VersionComparator comp;

    @Inject
    public MinVersionComparator(VersionComparator comp) {
      this.comp = comp;
    }

    @Override
    public int compare(VersionRange o1, VersionRange o2) {
      if (o1 == null) {
        if (o2 == null) {
          return 0;
        }
        return 1;
      }
      if (o2 == null) {
        return -1;
      }

      return comp.compare(o1.minVersion, o2.minVersion);
    }
  }

  @Singleton
  public static class MinVersionBuilderComparator implements Comparator<VersionRange.BUILDER> {

    private final VersionComparator comp;

    @Inject
    public MinVersionBuilderComparator(VersionComparator comp) {
      this.comp = comp;
    }

    @Override
    public int compare(VersionRange.BUILDER o1, VersionRange.BUILDER o2) {
      if (o1 == null) {
        if (o2 == null) {
          return 0;
        }
        return 1;
      }
      if (o2 == null) {
        return -1;
      }

      return comp.compare(o1.minVersion, o2.minVersion);
    }
  }
}

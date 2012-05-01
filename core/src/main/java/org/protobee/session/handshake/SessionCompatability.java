package org.protobee.session.handshake;

import java.util.Map;
import java.util.Set;

import org.protobee.compatability.VersionRange;
import org.protobee.guice.SessionScope;
import org.protobee.util.VersionComparator;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

@SessionScope
public class SessionCompatability {

  private Map<String, Set<VersionRange>> allowedVersions = null;

  private VersionComparator comp;

  @Inject
  public SessionCompatability(VersionComparator comp) {
    this.comp = comp;
  }

  public boolean isCompatible(String name, String version) {
    Preconditions.checkArgument(VersionComparator.isValidVersionString(version),
        "Invalid version string");
    Set<VersionRange> allowed = allowedVersions.get(name);
    if (allowed == null) {
      return false;
    }

    boolean fits = false;
    for (VersionRange range : allowed) {
      if (contains(range, version)) {
        fits = true;
      }
    }
    return fits;
  }

  private boolean contains(VersionRange range, String version) {
    return comp.compare(range.getMinVersion(), version) <= 0
        && comp.compare(version, range.getMaxVersion()) <= 0;
  }

  void setVersions(Map<String, Set<VersionRange>> allowedVersions) {
    this.allowedVersions = allowedVersions;
  }
}

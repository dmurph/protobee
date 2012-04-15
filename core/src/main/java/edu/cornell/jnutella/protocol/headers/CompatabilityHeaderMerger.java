package edu.cornell.jnutella.protocol.headers;

import java.util.Collection;
import java.util.Map;

import org.jboss.netty.handler.codec.http.HttpMessage;
import org.slf4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.util.VersionComparator;

public class CompatabilityHeaderMerger {

  @InjectLogger
  private Logger log;

  private final Multimap<String, CompatabilityHeader> requiredVersions = ArrayListMultimap.create();
  private final Multimap<String, CompatabilityHeader> requestedVersions = ArrayListMultimap
      .create();
  private final VersionComparator comp;

  @Inject
  public CompatabilityHeaderMerger(VersionComparator comparator) {
    comp = comparator;
  }

  public void addHeaders(Headers headers) {
    for (CompatabilityHeader header : headers.requiredCompatabilities()) {
      requiredVersions.put(header.name(), header);
    }
    for (CompatabilityHeader header : headers.requestedCompatabilities()) {
      requestedVersions.put(header.name(), header);
    }
  }

  public void populateModuleHeaders(HttpMessage message) {
    for (String name : requiredVersions.keySet()) {
      Collection<CompatabilityHeader> versions = requiredVersions.get(name);

      VersionRange version = null;
      for (CompatabilityHeader header : versions) {

        if (version == null) {
          version = new VersionRange(header.minVersion(), header.maxVersion());
          continue;
        }

        merge(version, header.minVersion(), header.maxVersion());
        if (comp.compare(version.minVersion, version.maxVersion) > 0) {
          throw new RuntimeException("Conflicting capabilities");
        }
      }
      String max = version.maxVersion;
      if (max.equals("+")) {
        max = version.minVersion;
      }
      log.debug("Merged required header '" + name + "' to range " + version
          + ", with decided version: " + max);
      message.addHeader(name, max);
    }
    for (String name : requestedVersions.keySet()) {
      if (message.containsHeader(name)) {
        continue;
      }
      Collection<CompatabilityHeader> versions = requestedVersions.get(name);

      VersionRange version = null;
      for (CompatabilityHeader header : versions) {

        if (version == null) {
          version = new VersionRange(header.minVersion(), header.maxVersion());
          continue;
        }

        merge(version, header.minVersion(), header.maxVersion());
        if (comp.compare(version.minVersion, version.maxVersion) > 0) {
          log.warn("Conflict in requested compabatilities for header: " + name
              + ".  Disabling feature.");
          version = null;
          break;
        }
      }
      if (version == null) {
        continue;
      }
      String max = version.maxVersion;
      if (max.equals("+")) {
        max = version.minVersion;
      }
      log.debug("Merged requested header '" + name + "' to range " + version
          + ", with decided version: " + max);
      message.addHeader(name, max);
    }
  }

  public Map<String, String> mergeHeaders(HttpMessage message) {
    ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();

    for (Map.Entry<String, String> header : message.getHeaders()) {
      String name = header.getKey();
      String value = header.getValue().trim();

      if (requiredVersions.containsKey(name)) {
        Collection<CompatabilityHeader> versions = requiredVersions.get(name);

        mergeToMax(builder, name, value, versions);
      } else if (requestedVersions.containsKey(name)) {
        Collection<CompatabilityHeader> versions = requestedVersions.get(name);

        mergeToMax(builder, name, value, versions);
      }
    }
    return builder.build();
  }


  private void mergeToMax(ImmutableMap.Builder<String, String> out, String name, String value,
      Collection<CompatabilityHeader> versions) {
    String version = value;
    for (CompatabilityHeader compHeader : versions) {

      if (compHeader.maxVersion().equals("+")) {
        if (comp.compare(compHeader.minVersion(), version) > 0) {
          version = null;
          break;
        }
        continue;
      }
      if (comp.compare(compHeader.maxVersion(), version) < 0) {
        version = compHeader.maxVersion();
      }
    }

    log.debug("Agreed version for header '" + name + "': " + version);
    out.put(name, version);
  }

  private void merge(VersionRange version, String minVersion, String maxVersion) {
    if (comp.compare(version.minVersion, minVersion) < 0) {
      version.minVersion = minVersion;
    }
    if (version.maxVersion.equals("+")) {
      if (maxVersion.equals("+")) {
        return;
      }
      version.maxVersion = maxVersion;
      return;
    }

    if (maxVersion.equals("+")) {
      return;
    }

    if (comp.compare(version.maxVersion, maxVersion) > 0) {
      version.maxVersion = maxVersion;
    }
  }

  private static class VersionRange {
    String minVersion;
    String maxVersion;

    public VersionRange(String minVersion, String maxVersion) {
      this.minVersion = minVersion;
      this.maxVersion = maxVersion;
    }

    @Override
    public String toString() {
      return minVersion + "-" + maxVersion;
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
  }
}

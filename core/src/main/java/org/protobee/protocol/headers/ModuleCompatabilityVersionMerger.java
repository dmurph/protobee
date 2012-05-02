package org.protobee.protocol.headers;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.handler.codec.http.HttpMessage;
import org.protobee.annotation.InjectLogger;
import org.protobee.compatability.VersionRange;
import org.protobee.compatability.VersionRangeMerger;
import org.protobee.guice.scopes.ProtocolScope;
import org.protobee.modules.ProtocolModule;
import org.protobee.session.SessionProtocolModules;
import org.protobee.session.SessionState;
import org.protobee.util.HeaderUtil;
import org.protobee.util.VersionComparator;
import org.protobee.util.VersionUtils;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Precomputes and provides merged compatibility headers for base set of all modules for a protocol,
 * and also merges the base set of headers with given http message headers, and also uses the
 * modified set of headers from the current session.
 * 
 * @author Daniel
 */
@ProtocolScope
public class ModuleCompatabilityVersionMerger {

  @InjectLogger
  private Logger log;

  private final ImmutableMultimap<String, VersionRange> requiredVersions;
  private final ImmutableMultimap<String, VersionRange> requestedVersions;
  private final ImmutableMultimap<String, VersionRange> excludedVersions;
  private final VersionComparator comp;
  private final VersionRangeMerger rangeMerger;

  private final Provider<SessionProtocolModules> modulesProvider;

  @Inject
  public ModuleCompatabilityVersionMerger(VersionComparator comparator,
      Set<Class<? extends ProtocolModule>> moduleClasses, VersionRangeMerger merger,
      Provider<SessionProtocolModules> modulesProvider) {
    this(comparator, Iterables.toArray(Iterables.transform(moduleClasses,
        new Function<Class<? extends ProtocolModule>, Headers>() {
          @Override
          public Headers apply(Class<? extends ProtocolModule> input) {
            Headers headers = input.getAnnotation(Headers.class);
            Preconditions.checkArgument(headers != null,
                "Every protocol module must have a @Headers annotation");
            return headers;
          }
        }), Headers.class), merger, modulesProvider);
  }

  @VisibleForTesting
  public ModuleCompatabilityVersionMerger(VersionComparator comparator, Headers[] headersArray,
      VersionRangeMerger merger, Provider<SessionProtocolModules> modulesProvider) {
    comp = comparator;
    this.rangeMerger = merger;
    this.modulesProvider = modulesProvider;

    ImmutableMultimap.Builder<String, VersionRange> requiredVersionsBuilder =
        ImmutableMultimap.builder();
    ImmutableMultimap.Builder<String, VersionRange> requestedVersionsBuilder =
        ImmutableMultimap.builder();
    ImmutableMultimap.Builder<String, VersionRange> excludedVersionsBuilder =
        ImmutableMultimap.builder();

    HashMultimap<String, VersionRange> tempMap = HashMultimap.create();

    for (Headers headers : headersArray) {
      for (CompatabilityHeader header : headers.excluding()) {
        tempMap.put(header.name(), new VersionRange(header));
      }
    }
    for (String name : tempMap.keySet()) {
      Set<VersionRange> ranges = tempMap.get(name);
      Set<VersionRange> combined = rangeMerger.union(ranges);
      excludedVersionsBuilder.putAll(name, combined);
    }

    excludedVersions = excludedVersionsBuilder.build();

    tempMap.clear();

    for (Headers headers : headersArray) {
      for (CompatabilityHeader header : headers.required()) {
        tempMap.put(header.name(), new VersionRange(header));
      }
    }

    mergeSubExcluded(requiredVersionsBuilder, tempMap);

    tempMap.clear();

    for (Headers headers : headersArray) {
      for (CompatabilityHeader header : headers.requested()) {
        tempMap.put(header.name(), new VersionRange(header));
      }
    }

    mergeSubExcluded(requestedVersionsBuilder, tempMap);

    requiredVersions = requiredVersionsBuilder.build();
    requestedVersions = requestedVersionsBuilder.build();
  }

  private void mergeSubExcluded(
      ImmutableMultimap.Builder<String, VersionRange> mergedOutputBuilder,
      HashMultimap<String, VersionRange> tempMap) {
    for (String name : tempMap.keySet()) {
      Set<VersionRange> ranges = tempMap.get(name);
      VersionRange intersection = rangeMerger.intersection(ranges);
      Preconditions
          .checkArgument(intersection != null, "Conflicting required versions for " + name);

      Set<VersionRange> subExclusions =
          rangeMerger.subtract(intersection, excludedVersions.get(name));

      Preconditions.checkArgument(subExclusions != null,
          "Conflicting required and excluded versions for " + name);
      mergedOutputBuilder.putAll(name, subExclusions);
    }
  }

  private String findMaxVersion(Collection<VersionRange> versions) {
    String max = null;
    for (VersionRange versionRange : versions) {
      String currMax =
          versionRange.getMaxVersion().equals(VersionRange.PLUS)
              ? versionRange.getMinVersion()
              : versionRange.getMaxVersion();
      if (max == null) {
        max = currMax;
      } else if (comp.compare(currMax, max) > 0) {
        max = currMax;
      }
    }
    return max;
  }

  public void populateModuleHeaders(HttpMessage message) {
    for (String name : requiredVersions.keySet()) {
      Collection<VersionRange> versions = requiredVersions.get(name);
      Preconditions.checkState(!versions.isEmpty(), "Versions cannot be empty");
      message.addHeader(name, findMaxVersion(versions));
    }
    for (String name : requestedVersions.keySet()) {
      if (message.containsHeader(name)) {
        continue;
      }
      Collection<VersionRange> versions = requestedVersions.get(name);
      Preconditions.checkState(!versions.isEmpty(), "Versions cannot be empty");
      message.addHeader(name, findMaxVersion(versions));
    }
  }

  /**
   * Precondition: must be in session scope
   */
  public Map<String, String> mergeHandshakeHeaders(HttpMessage message, SessionState handshakeState) {
    switch (handshakeState) {
      case HANDSHAKE_0:
        return mergeForAllModules(message);
      case HANDSHAKE_1:
        return mergeForMutableModules(message);
      default:
        throw new IllegalStateException(
            "No compatability handshake merging should be happening in handshake state "
                + handshakeState);
    }
  }

  private Map<String, String> mergeForAllModules(HttpMessage message) {
    Map<String, String> result = Maps.newHashMap();

    for (Map.Entry<String, String> header : message.getHeaders()) {
      String name = header.getKey();
      String value = header.getValue().trim();

      String version;
      if (requiredVersions.containsKey(name)) {
        Collection<VersionRange> versions = requiredVersions.get(name);

        version = VersionUtils.findSmallestMax(value, versions, rangeMerger, comp);
      } else if (requestedVersions.containsKey(name)) {
        Collection<VersionRange> versions = requestedVersions.get(name);

        version = VersionUtils.findSmallestMax(value, versions, rangeMerger, comp);
      } else {
        log.debug("We have no compatability need for header: " + name);
        continue;
      }

      if (version == null) {
        log.debug("Couldn't agree on version for header: " + name);
      } else {
        log.debug("Agreed version for header '" + name + "': " + version);
        result.put(name, version);
      }
    }
    return result;
  }

  private Map<String, String> mergeForMutableModules(HttpMessage message) {

    Set<ProtocolModule> modules = modulesProvider.get().getMutableModules();

    Headers[] headerArray = HeaderUtil.getHeadersFromModules(modules);

    Map<String, String> result = Maps.newHashMap();

    HashMultimap<String, VersionRange> excluding = HeaderUtil.getExcludingVersions(headerArray);

    // merge our exclusions for scalable performance
    for (String name : excluding.keySet()) {
      Set<VersionRange> versions = excluding.get(name);
      Set<VersionRange> updated = rangeMerger.union(versions);
      excluding.replaceValues(name, updated);
    }

    HashMultimap<String, VersionRange> required = HeaderUtil.getRequiredVersions(headerArray);

    for (String name : required.keySet()) {
      if (!message.containsHeader(name)) {
        continue;
      }

      Set<VersionRange> ranges = required.get(name);
      String previous = message.getHeader(name);

      if (excluding.containsKey(name)) {
        VersionRange intersection = rangeMerger.intersection(ranges);
        Preconditions.checkState(intersection != null,
            "Conflicting required versions for compatability: " + name);
        ranges = rangeMerger.subtract(intersection, excluding.get(name));
      }

      String value = VersionUtils.findSmallestMax(previous, ranges, rangeMerger, comp);

      log.debug("Merged given header " + name + ": " + previous + " to " + value);
      result.put(name, value);
    }

    HashMultimap<String, VersionRange> requested = HeaderUtil.getRequestedVersions(headerArray);
    for (String name : requested.keySet()) {
      if (!message.containsHeader(name) || result.containsKey(name)) {
        continue;
      }

      Set<VersionRange> ranges = requested.get(name);
      String previous = message.getHeader(name);

      if (excluding.containsKey(name)) {
        VersionRange intersection = rangeMerger.intersection(ranges);
        Preconditions.checkState(intersection != null,
            "Conflicting required versions for compatability: " + name);
        ranges = rangeMerger.subtract(intersection, excluding.get(name));
      }

      String value = VersionUtils.findSmallestMax(previous, ranges, rangeMerger, comp);

      log.debug("Merged given header " + name + ": " + previous + " to " + value);
      result.put(name, value);
    }

    return result;
  }
}

package org.protobee.compatability;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.protobee.compatability.VersionRange.BUILDER;
import org.protobee.compatability.VersionRange.MinVersionBuilderComparator;
import org.protobee.compatability.VersionRange.MinVersionComparator;
import org.protobee.util.VersionComparator;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class VersionRangeMerger {

  private final VersionComparator comp;
  private final MinVersionBuilderComparator minBuilderComp;
  private final MinVersionComparator minComp;

  @Inject
  public VersionRangeMerger(VersionComparator comp, MinVersionBuilderComparator minBuilderComp,
      MinVersionComparator minComp) {
    this.comp = comp;
    this.minBuilderComp = minBuilderComp;
    this.minComp = minComp;
  }

  @VisibleForTesting
  public VersionRangeMerger() {
    this.comp = new VersionComparator();
    this.minBuilderComp = new MinVersionBuilderComparator(comp);
    this.minComp = new MinVersionComparator(comp);
  }

  public VersionRange intersection(Set<VersionRange> entries) {
    VersionRange.BUILDER builder = null;

    for (VersionRange entry : entries) {
      if (builder == null) {
        builder = VersionRange.builder().set(entry);
        continue;
      }

      if (comp.compare(builder.getMinVersion(), entry.getMinVersion()) < 0) {
        builder.setMinVersion(entry.getMinVersion());
      }

      if (builder.getMaxVersion().equals(VersionRange.PLUS)) {
        if (entry.getMaxVersion().equals(VersionRange.PLUS)) {
          continue;
        }
        builder.setMaxVersion(entry.getMaxVersion());
        continue;
      }

      if (entry.getMaxVersion().equals(VersionRange.PLUS)) {
        continue;
      }

      if (comp.compare(builder.getMaxVersion(), entry.getMaxVersion()) > 0) {
        builder.setMaxVersion(entry.getMaxVersion());
      }

      if (comp.compare(builder.getMinVersion(), builder.getMaxVersion()) > 0) {
        return null;
      }
    }
    return builder.build();
  }

  public Set<VersionRange> union(Set<VersionRange> entries) {
    List<VersionRange.BUILDER> builders =
        Lists.newArrayList(Iterables.transform(entries,
            new Function<VersionRange, VersionRange.BUILDER>() {
              @Override
              public BUILDER apply(VersionRange input) {
                return VersionRange.builder().set(input);
              }
            }));
    Collections.sort(builders, minBuilderComp);

    Set<VersionRange> union = Sets.newHashSet();

    final int size = builders.size();
    VersionRange.BUILDER currBuilder = null;
    for (int i = 0; i < size; i++) {
      VersionRange.BUILDER builder = builders.get(i);
      if (currBuilder == null) {
        currBuilder = builder;
        continue;
      }

      if (currBuilder.getMaxVersion().equals(VersionRange.PLUS)) {
        break;
      }
      if (comp.compare(currBuilder.getMaxVersion(), builder.getMinVersion()) < 0) {
        union.add(currBuilder.build());
        currBuilder = builder;
        continue;
      }
      if (builder.getMaxVersion().equals(VersionRange.PLUS)) {
        currBuilder.setMaxVersion(VersionRange.PLUS);
        continue;
      }
      if (comp.compare(currBuilder.getMaxVersion(), builder.getMaxVersion()) < 0) {
        currBuilder.setMaxVersion(builder.getMaxVersion());
      }
    }
    if (currBuilder != null) {
      union.add(currBuilder.build());
    }
    return union;
  }

  /**
   * Returned set is sorted by
   * 
   * @param range
   * @param toSubstract
   * @return
   */
  public Set<VersionRange> subtract(VersionRange range, Collection<VersionRange> toSubstract) {
    List<VersionRange> subtract = Lists.newArrayList(toSubstract);
    Collections.sort(subtract, minComp);

    Set<VersionRange> result = Sets.newHashSet();

    VersionRange.BUILDER builder = VersionRange.builder().set(range);

    // we don't just exit, because we could have split up the input range, and then
    // cover the second range
    boolean rangeCovered = false;
    for (VersionRange versionRange : subtract) {
      String minVersion = versionRange.getMinVersion();
      String maxVersion = versionRange.getMaxVersion();

      if (comp.compare(minVersion, builder.getMinVersion()) < 0) {
        if (!maxVersion.equals(VersionRange.PLUS)
            && (builder.getMaxVersion().equals(VersionRange.PLUS) || comp.compare(maxVersion,
                builder.getMinVersion()) > 0)) {
          builder.setMinVersion(maxVersion);
        }
        else if (maxVersion.equals(VersionRange.PLUS)
            || comp.compare(maxVersion, builder.getMaxVersion()) > 0) {
          rangeCovered = true;
          break;
        }
      } else if (builder.getMaxVersion().equals(VersionRange.PLUS)) {
        builder.setMaxVersion(minVersion);
      } else if (maxVersion.equals(VersionRange.PLUS)
          || comp.compare(maxVersion, builder.getMaxVersion()) > 0) {

        if (comp.compare(minVersion, builder.getMaxVersion()) < 0) {
          builder.setMaxVersion(minVersion);
        } else {
          // it's above us, don't do anything
        }
      } else {
        // we are contained within.
        String oldMax = builder.getMaxVersion();
        builder.setMaxVersion(minVersion);
        result.add(builder.build());
        builder = VersionRange.builder().setMinVersion(maxVersion).setMaxVersion(oldMax);
      }
      if (!builder.getMaxVersion().equals(VersionRange.PLUS) && comp.compare(builder.getMinVersion(), builder.getMaxVersion()) > 0) {
        rangeCovered = true;
        break;
      }
    }
    if (rangeCovered) {
      return result.isEmpty() ? null : result;
    }

    result.add(builder.build());
    return result;
  }

  public boolean contains(String version, VersionRange range) {
    Preconditions.checkArgument(VersionComparator.isValidVersionString(version),
        "Not a valid version string");
    Preconditions.checkNotNull(range);
    return comp.compare(version, range.getMinVersion()) >= 0
        && (range.getMaxVersion().equals(VersionRange.PLUS) || comp.compare(version,
            range.getMaxVersion()) <= 0);
  }
}

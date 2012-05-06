package org.protobee.session.handshake;

import java.util.Iterator;
import java.util.Map;

import org.protobee.compatability.CompatabilityHeader;
import org.protobee.compatability.Headers;
import org.protobee.compatability.VersionRange;
import org.protobee.guice.scopes.SessionScope;
import org.protobee.modules.ProtocolModule;
import org.protobee.session.SessionProtocolModules;
import org.protobee.util.ArrayIterator;
import org.protobee.util.CombinedIterator;
import org.protobee.util.VersionComparator;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

/**
 * Filters the current modules based on the given http headers, and removes the filtered modules
 * from the event bus
 * 
 * @author Daniel
 */
@SessionScope
public class ProtocolModuleFilter {

  private final VersionComparator comp;
  private final SessionProtocolModules modules;
  private final EventBus eventBus;

  @Inject
  public ProtocolModuleFilter(VersionComparator comp, SessionProtocolModules modules,
      EventBus eventBus) {
    this.comp = comp;
    this.modules = modules;
    this.eventBus = eventBus;
  }

  public String getFilterModulesString() {
    return modules.getMutableModules().toString();
  }

  public void filterModules(Map<String, String> httpHeaders) {
    Iterator<ProtocolModule> it = modules.getMutableModules().iterator();

    while (it.hasNext()) {
      ProtocolModule module = it.next();
      Headers headers = module.getHeaders();

      if (headers == null) {
        continue;
      }
      for (CompatabilityHeader header : headers.required()) {
        if (!httpHeaders.containsKey(header.name())) {
          eventBus.unregister(module);
          it.remove();
          continue;
        }
        String value = httpHeaders.get(header.name());

        if (comp.compare(header.minVersion(), value) > 0
            || (!header.maxVersion().equals("+") && comp.compare(header.maxVersion(), value) < 0)) {
          eventBus.unregister(module);
          it.remove();
          continue;
        }
      }
      Iterator<CompatabilityHeader> excluding =
          new CombinedIterator<CompatabilityHeader>(new ArrayIterator<CompatabilityHeader>(
              headers.excluding()), new ArrayIterator<CompatabilityHeader>(
              headers.silentExcluding()));
      while (excluding.hasNext()) {
        CompatabilityHeader header = excluding.next();
        if (!httpHeaders.containsKey(header.name())) {
          continue;
        }
        String value = httpHeaders.get(header.name());

        String minVersion = header.minVersion();
        String maxVersion = header.maxVersion();
        Preconditions.checkArgument(VersionComparator.isValidVersionString(minVersion),
            "Invalid header min version" + minVersion);
        Preconditions.checkArgument(
            maxVersion.equals(VersionRange.PLUS)
                || VersionComparator.isValidVersionString(maxVersion),
            "Invalid header max version " + maxVersion);

        if (comp.compare(minVersion, value) <= 0
            && (maxVersion.equals("+") || comp.compare(maxVersion, value) >= 0)) {
          eventBus.unregister(module);
          it.remove();
          continue;
        }
      }
    }
  }
}

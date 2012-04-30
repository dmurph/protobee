package org.protobee.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.protobee.modules.ProtocolModule;
import org.protobee.protocol.headers.CompatabilityHeader;
import org.protobee.protocol.headers.Headers;

import com.google.common.base.Predicate;
import com.google.inject.Inject;


public class ProtocolModuleFilter {

  private final VersionComparator comp;

  @Inject
  public ProtocolModuleFilter(VersionComparator comp) {
    this.comp = comp;
  }

  public void filterModules(Set<ProtocolModule> modules, Map<String, String> httpHeaders,
      Predicate<ProtocolModule> confirm) {
    Iterator<ProtocolModule> it = modules.iterator();

    while (it.hasNext()) {
      ProtocolModule module = it.next();
      Headers headers = module.getClass().getAnnotation(Headers.class);

      if (headers == null) {
        continue;
      }
      for (CompatabilityHeader header : headers.required()) {
        if (!httpHeaders.containsKey(header.name())) {
          if (confirm.apply(module)) {
            it.remove();
            break;
          } else {
            continue;
          }
        }
        String value = httpHeaders.get(header.name());

        if (comp.compare(header.minVersion(), value) > 0
            || (!header.maxVersion().equals("+") && comp.compare(header.maxVersion(), value) < 0)) {
          if (confirm.apply(module)) {
            it.remove();
            break;
          } else {
            continue;
          }
        }
      }
    }
  }
}

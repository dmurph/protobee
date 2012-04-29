package edu.cornell.jnutella.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.ProtocolConfig;

public class ProtocolConfigUtils {

  private static final Logger log = LoggerFactory.getLogger(ProtocolConfigUtils.class);

  public static Map<String, Object> mergeNettyBindOptions(Set<ProtocolConfig> configs) {
    Map<String, Object> result = Maps.newHashMap();
    for (ProtocolConfig protocolConfig : configs) {
      Map<String, Object> options = protocolConfig.getNettyBootstrapOptions();
      for (Entry<String, Object> optionEntry : options.entrySet()) {
        String name = optionEntry.getKey();
        Object option = result.get(name);
        Object newOption = optionEntry.getValue();
        if (option != null && !option.equals(newOption)) {
          log.warn("Option '" + name + "' is already definited to be '" + option
              + "'.  Overwriting with '" + newOption + " from " + protocolConfig);
        }
        result.put(name, newOption);
      }
    }
    return result;
  }

  public static Set<Protocol> getProtocolSet(Set<ProtocolConfig> configs) {
    return ImmutableSet.copyOf(Iterables.transform(configs,
        new Function<ProtocolConfig, Protocol>() {
          @Override
          public Protocol apply(ProtocolConfig input) {
            return input.get();
          }
        }));
  }
}

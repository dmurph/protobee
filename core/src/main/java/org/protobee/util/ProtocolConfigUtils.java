package org.protobee.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolConfig;
import org.protobee.protocol.ProtocolModel;
import org.protobee.protocol.ServerOptionsMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.inject.Provider;


public class ProtocolConfigUtils {

  private static final Logger log = LoggerFactory.getLogger(ProtocolConfigUtils.class);

  public static Map<String, Object> mergeNettyBindOptions(Set<ProtocolModel> models) {
    Preconditions.checkNotNull(models);
    Map<String, Object> result = Maps.newHashMap();
    for (ProtocolModel model : models) {
      Map<String, Object> options = model.getServerOptions();
      for (Entry<String, Object> optionEntry : options.entrySet()) {
        String name = optionEntry.getKey();
        Object option = result.get(name);
        Object newOption = optionEntry.getValue();
        if (option != null && !option.equals(newOption)) {
          log.warn("Option '" + name + "' is already definited to be '" + option
              + "'.  Overwriting with '" + newOption + " from " + model);
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

  public static Set<Protocol> getProtocolSetFromModels(Set<ProtocolModel> models) {
    return ImmutableSet.copyOf(Iterables.transform(models, new Function<ProtocolModel, Protocol>() {
      @Override
      public Protocol apply(ProtocolModel input) {
        return input.getProtocol();
      }
    }));
  }
}

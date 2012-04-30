package org.protobee.guice;

import java.util.List;
import java.util.ServiceLoader;

import org.protobee.ProtobeeGuiceModule;
import org.protobee.plugin.OverridingModule;
import org.protobee.plugin.PluginGuiceModule;

import com.google.common.collect.Lists;
import com.google.inject.Module;
import com.google.inject.util.Modules;


public class ProtobeeModuleCombiner {
  
  /**
   * Gets all plugin modules with main application module
   * 
   * @return
   */
  public static Module getCombinedModules() {
    List<Module> modules = Lists.newArrayList();
    List<Module> overridingModules = Lists.newArrayList();

    modules.add(new ProtobeeGuiceModule());

    ServiceLoader<PluginGuiceModule> pluginModules = ServiceLoader.load(PluginGuiceModule.class);
    for (PluginGuiceModule pluginModule : pluginModules) {
      if (pluginModule.getClass().getAnnotation(OverridingModule.class) != null) {
        overridingModules.add(pluginModule);
      } else {
        modules.add(pluginModule);
      }
    }

    return Modules.override(modules).with(overridingModules);
  }
}

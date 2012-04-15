package edu.cornell.jnutella;

import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.Bootstrap;
import org.jboss.netty.channel.ChannelFactory;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import edu.cornell.jnutella.guice.JnutellaMainModule;
import edu.cornell.jnutella.guice.netty.ExecutorModule;
import edu.cornell.jnutella.network.ReceivingRequestMultiplexer;
import edu.cornell.jnutella.plugin.OverridingModule;
import edu.cornell.jnutella.plugin.PluginGuiceModule;

/**
 * Controller for using jnutella
 * 
 * @author Daniel
 */
public class JNutella {

  private final ChannelFactory channelFactory;
  private final Bootstrap bootstrap;
  private final ReceivingRequestMultiplexer multiplexer;


  @Inject
  public JNutella(ChannelFactory channelFactory, Bootstrap bootstrap,
      ReceivingRequestMultiplexer multiplexer) {
    this.channelFactory = channelFactory;
    this.bootstrap = bootstrap;
    this.multiplexer = multiplexer;
    this.bootstrap.setFactory(channelFactory);
  }

  public void startup() {

  }

  public void shutdown() {

  }

  /**
   * @return the modules of all application modules and plugin modules, with default
   *         {@link Executor}s
   */
  public static Module getCombinedModules() {
    return getCombinedModules(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
  }

  /**
   * @param bossExecutor the boss executor to use for the executor module
   * @param workerExecutor the worker executor to use for the worker module
   * @return the modules of all application modules and plugin modules
   */
  public static Module getCombinedModules(Executor bossExecutor, Executor workerExecutor) {
    List<Module> modules = Lists.newArrayList();
    List<Module> overridingModules = Lists.newArrayList();

    modules.add(new ExecutorModule(bossExecutor, workerExecutor));
    modules.add(new JnutellaMainModule());

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

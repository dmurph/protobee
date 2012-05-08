package org.protobee;

import org.protobee.util.DependencyGraphGenerator;

import com.google.common.collect.Sets;
import com.google.inject.Guice;

public class DependencyGraphGeneratorProxy {
  public static void main(String[] args) {
    DependencyGraphGenerator.graphGood("ProtobeeCoreDependencyGraph",
      Guice.createInjector(new ProtobeeGuiceModule()),
        Sets.newHashSet("SessionModel", "ProtocolModel", "NetworkIdentity", "ProtocolConfig"));
  }
}

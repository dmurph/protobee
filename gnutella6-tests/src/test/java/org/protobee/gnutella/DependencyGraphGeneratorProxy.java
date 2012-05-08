package org.protobee.gnutella;

import org.protobee.ProtobeeGuiceModule;
import org.protobee.util.DependencyGraphGenerator;

import com.google.common.collect.Sets;
import com.google.inject.Guice;

public class DependencyGraphGeneratorProxy {
  public static void main(String[] args) {
    DependencyGraphGenerator.graphGood("depGraph",
        Guice.createInjector(new ProtobeeGuiceModule(), new GnutellaGuiceModule()),
        Sets.newHashSet("SessionModel", "ProtocolModel", "NetworkIdentity"));
  }
}

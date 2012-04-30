package org.protobee;

import org.protobee.util.DependencyGraphGenerator;

import com.google.inject.Guice;

public class DependencyGraphGeneratorProxy {
  public static void main(String[] args) {
    DependencyGraphGenerator.graphGood("depGraph", Guice.createInjector(new ProtobeeGuiceModule()));
  }
}

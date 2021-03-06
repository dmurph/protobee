package org.protobee;

import org.protobee.examples.broadcast.BroadcastGuiceModule;
import org.protobee.examples.emotion.EmotionGuiceModule;
import org.protobee.util.DependencyGraphGenerator;

import com.google.common.collect.Sets;
import com.google.inject.Guice;

public class ExamplesGraphGeneratorProxy {
  public static void main(String[] args) {
    DependencyGraphGenerator.graphGood("broadcast",
        Guice.createInjector(new ProtobeeGuiceModule(), new BroadcastGuiceModule(), new EmotionGuiceModule()),
        Sets.newHashSet("SessionModel", "ProtocolModel", "NetworkIdentity", "ProtocolConfig"));
  }
}

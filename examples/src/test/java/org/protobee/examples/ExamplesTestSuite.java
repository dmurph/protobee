package org.protobee.examples;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.protobee.examples.broadcast.BroadcastTestSuite;

@RunWith(Suite.class)
@SuiteClasses({BroadcastTestSuite.class})
public class ExamplesTestSuite {}

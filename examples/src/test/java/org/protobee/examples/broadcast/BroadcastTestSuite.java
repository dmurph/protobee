package org.protobee.examples.broadcast;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({BroadcastHandshakeTest.class, BroadcastProtocolTests.class})
public class BroadcastTestSuite {

}

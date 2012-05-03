package org.protobee.protocol;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({HandshakeUpstreamTests.class, PostHandlerTest.class})
public class ProtocolTestSuite {
}

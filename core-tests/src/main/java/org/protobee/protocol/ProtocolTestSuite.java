package org.protobee.protocol;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.protobee.protocol.headers.HeaderMergerTest;


@RunWith(Suite.class)
@SuiteClasses({HandshakeUpstreamTests.class, HeaderMergerTest.class})
public class ProtocolTestSuite {

}

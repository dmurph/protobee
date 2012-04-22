package edu.cornell.jnutella.protocol;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import edu.cornell.jnutella.protocol.headers.HeaderMergerTest;

@RunWith(Suite.class)
@SuiteClasses({HandshakeUpstreamTests.class, HeaderMergerTest.class})
public class ProtocolTestSuite {

}

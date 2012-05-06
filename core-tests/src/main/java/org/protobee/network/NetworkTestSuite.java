package org.protobee.network;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({HandshakeBootstrapperTest.class, ConnectionBinderTest.class, RequestReceiverTest.class})
public class NetworkTestSuite {

}

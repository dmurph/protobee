package edu.cornell.jnutella.gnutella.messages;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({EncodingHandlerTest.class, GGEPTest.class, PingTest.class, PongTest.class, QueryTest.class, PushTest.class})
public class MessagesTestSuite {
}

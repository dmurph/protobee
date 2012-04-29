package org.protobee.gnutella.messages;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({EncodingHandlerTest.class, EQHDTest.class, GGEPTest.class, PingTest.class, 
  PongTest.class, PushTest.class, QueryHitTest.class, QueryTest.class, ResponseTest.class,
  RoutingTest.class})
public class MessagesTestSuite {
}

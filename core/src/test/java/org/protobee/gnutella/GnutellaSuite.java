package org.protobee.gnutella;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.protobee.gnutella.messages.MessagesTestSuite;
import org.protobee.gnutella.modules.ModulesTestSuite;


@RunWith(Suite.class)
@SuiteClasses({MessagesTestSuite.class, ModulesTestSuite.class})
public class GnutellaSuite {

}

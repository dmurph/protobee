package edu.cornell.jnutella.gnutella;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import edu.cornell.jnutella.gnutella.messages.MessagesTestSuite;
import edu.cornell.jnutella.gnutella.modules.ModulesTestSuite;

@RunWith(Suite.class)
@SuiteClasses({MessagesTestSuite.class, ModulesTestSuite.class})
public class GnutellaSuite {

}

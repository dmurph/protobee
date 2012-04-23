package edu.cornell.jnutella.gnutella;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import edu.cornell.jnutella.gnutella.messages.MessagesTestSuite;

@RunWith(Suite.class)
@SuiteClasses({MessagesTestSuite.class})
public class GnutellaSuite {

}

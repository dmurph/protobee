package edu.cornell.jnutella;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import edu.cornell.jnutella.messages.MessagesTestSuite;

@RunWith(Suite.class)
@SuiteClasses({MessagesTestSuite.class})
public class CoreTestSuite {}

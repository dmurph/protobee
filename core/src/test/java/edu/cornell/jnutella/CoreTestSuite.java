package edu.cornell.jnutella;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import edu.cornell.jnutella.gnutella.GnutellaSuite;
import edu.cornell.jnutella.protocol.headers.HeaderMergerTest;
import edu.cornell.jnutella.util.UtilSuite;

@RunWith(Suite.class)
@SuiteClasses({GnutellaSuite.class, UtilSuite.class, HeaderMergerTest.class})
public class CoreTestSuite {}

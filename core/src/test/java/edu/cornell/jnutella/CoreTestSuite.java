package edu.cornell.jnutella;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import edu.cornell.jnutella.gnutella.GnutellaSuite;
import edu.cornell.jnutella.integrity.IntegrityTestSuite;
import edu.cornell.jnutella.network.NetworkTestSuite;
import edu.cornell.jnutella.protocol.ProtocolTestSuite;
import edu.cornell.jnutella.util.UtilSuite;

@RunWith(Suite.class)
@SuiteClasses({GnutellaSuite.class, UtilSuite.class, NetworkTestSuite.class,
    ProtocolTestSuite.class, IntegrityTestSuite.class, IdentityTests.class})
public class CoreTestSuite {}

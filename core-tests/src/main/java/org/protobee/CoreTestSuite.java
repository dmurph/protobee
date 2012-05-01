package org.protobee;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.protobee.compatability.CompatabilityTestSuite;
import org.protobee.integrity.IntegrityTestSuite;
import org.protobee.network.NetworkTestSuite;
import org.protobee.protocol.ProtocolTestSuite;
import org.protobee.util.UtilSuite;


@RunWith(Suite.class)
@SuiteClasses({CompatabilityTestSuite.class, UtilSuite.class, NetworkTestSuite.class,
    ProtocolTestSuite.class, IntegrityTestSuite.class, IdentityTests.class,
    ServantBootstrapTest.class})
public class CoreTestSuite {}

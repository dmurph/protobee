package org.protobee.compatability;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({VersionMergerTest.class, ProtocolModuleFilterTests.class, HeaderMergerTest.class})
public class CompatabilityTestSuite {

}

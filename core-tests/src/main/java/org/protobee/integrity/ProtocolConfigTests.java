package org.protobee.integrity;

import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;
import org.protobee.protocol.ProtocolConfig;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;


public class ProtocolConfigTests extends AbstractIntegrityTest {

  @Test
  public void testSingletons() {
    Set<ProtocolConfig> configs =
        injector.getInstance(Key.get(new TypeLiteral<Set<ProtocolConfig>>() {}));
    Set<ProtocolConfig> configs2 =
        injector.getInstance(Key.get(new TypeLiteral<Set<ProtocolConfig>>() {}));

    for (ProtocolConfig protocolConfig : configs) {

      boolean found = false;
      for (ProtocolConfig protocolConfig2 : configs2) {
        if (protocolConfig == protocolConfig2) {
          found = true;
        }
      }
      assertTrue(protocolConfig + "isn't a singleton", found);
    }
  }

}

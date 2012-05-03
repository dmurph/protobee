package org.protobee.compatability;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Headers {
  /**
   * Capabilities required for this module to be included
   */
  CompatabilityHeader[] required();

  /**
   * Capabilities requested but not required
   */
  CompatabilityHeader[] requested() default {};

  /**
   * Capabilities that this module cannot load with, and will actively try to block on protocol
   * handshaking. These ranges are exclusive. That means the min and max versions are allowed, but
   * anything between them is not.
   * 
   * @return
   */
  CompatabilityHeader[] excluding() default {};

  /**
   * Capabilities that this module cannot load with, but will not effect the the versions chosen in
   * the handshake. These ranges are exclusive. That means the min and max versions are allowed, but
   * anything between them is not.
   * 
   * @return
   */
  CompatabilityHeader[] silentExcluding() default {};
}

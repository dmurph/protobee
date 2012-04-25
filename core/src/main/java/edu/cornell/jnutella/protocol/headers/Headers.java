package edu.cornell.jnutella.protocol.headers;

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
  CompatabilityHeader[] requested();
}

package edu.cornell.jnutella.protocol.headers;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CompatabilityHeader {
  String name();

  String minVersion();

  String maxVersion();
}

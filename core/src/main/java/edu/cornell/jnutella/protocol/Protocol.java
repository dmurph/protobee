package edu.cornell.jnutella.protocol;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a protocol to use in the jnutella framework
 * 
 * @author Daniel
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Protocol {
  /**
   * The name of the protocol, used in the header
   * 
   * @return
   */
  String name();

  /**
   * The version of the protocol, used to make the header
   * 
   * @return
   */
  int majorVersion();
  
  int minorVersion();

  /**
   * Regex string used to match each protocol to corresponding headers
   * 
   * @return
   */
  String headerRegex();
}

package org.protobee.guice.scopes;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

/**
 * Annotation for specifying a new protocol scope holder, or a new scope map for the protocol scope
 * 
 * @author Daniel
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@BindingAnnotation
public @interface NewProtocolScopeHolder {}

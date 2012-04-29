package org.protobee.guice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Provider;
import com.google.inject.ProvisionException;

public class PrescopedProvider<T> implements Provider<T> {

  private final Logger log = LoggerFactory.getLogger(PrescopedProvider.class);
  private final T dummy;
  private final String typeString;
  private final boolean fail;

  public PrescopedProvider() {
    typeString = null;
    fail = true;
    dummy = null;
  }

  public PrescopedProvider(String typeString) {
    fail = true;
    this.dummy = null;
    this.typeString = typeString;
  }

  public PrescopedProvider(T nonscopedValue, String typeString) {
    dummy = nonscopedValue;
    fail = false;
    this.typeString = typeString;
  }

  @Override
  public T get() {
    if (fail) {
      throw new ProvisionException("This provider should never be called, object "
          + (typeString == null ? "" : typeString) + " should be seeded in scope map");
    }

    log.info(typeString + " wasn't scoped, returning specified default " + dummy);
    return dummy;
  }

}

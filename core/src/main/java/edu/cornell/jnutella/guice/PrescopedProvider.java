package edu.cornell.jnutella.guice;

import com.google.inject.Provider;
import com.google.inject.ProvisionException;

public class PrescopedProvider<T> implements Provider<T> {

  @Override
  public T get() {
    throw new ProvisionException(
        "This provider should never be called, object should be seeded in scope map");
  }

}

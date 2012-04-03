package edu.cornell.jnutella.util;

import java.util.Map;


public class NameValue<V> implements Map.Entry<String, V> {

  private final String name;
  private V value;

  /**
   * Creates a new NameValue with a null value.
   */
  public NameValue(String name) {
    this(name, null);
  }

  /** Creates new NameValue */
  public NameValue(String name, V value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public String getKey() {
    return name;
  }

  public V getValue() {
    return value;
  }

  public V setValue(V value) {
    V old = this.value;
    this.value = value;
    return old;
  }

  @Override
  public String toString() {
    return "{name: " + name + ", value: " + value + "}";
  }
}

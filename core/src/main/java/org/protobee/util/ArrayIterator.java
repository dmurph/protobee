package org.protobee.util;

import java.util.Iterator;

public class ArrayIterator<T> implements Iterator<T> {
  private final T[] array;
  private int currentIndex = 0;

  public ArrayIterator(T[] array) {
    this.array = array;
  }

  public boolean hasNext() {
    return currentIndex < array.length;
  }

  public T next() {
    return array[currentIndex++];
  }

  public void remove() {
    throw new UnsupportedOperationException("cannot remove items from an array");
  }
}

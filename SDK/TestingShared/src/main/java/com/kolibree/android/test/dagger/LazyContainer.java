package com.kolibree.android.test.dagger;

import dagger.Lazy;

/** Created by miguelaragues on 14/9/17. */
public class LazyContainer<T> implements Lazy<T> {

  private final T object;

  private LazyContainer(T object) {
    this.object = object;
  }

  public static <K> Lazy<K> create(K facade) {
    return new LazyContainer<>(facade);
  }

  @Override
  public T get() {
    return object;
  }
}

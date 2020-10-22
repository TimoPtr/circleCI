package com.kolibree.android.test;

import androidx.fragment.app.FragmentActivity;

/** Created by miguelaragues on 8/8/17. */
public class KolibreeActivityTestRule<T extends FragmentActivity> extends BaseActivityTestRule<T> {

  private KolibreeActivityTestRule(Class<T> activityClass, boolean launchActivity) {
    super(activityClass, launchActivity);
  }

  /** InternalBuilder to isolate creation of TestRule from the world. */
  public static class Builder<K extends FragmentActivity> extends InnerBuilder<K, Builder<K>> {

    public Builder(Class<K> activityClass) {
      super(activityClass);
    }

    public KolibreeActivityTestRule<K> build() {
      if (launchActivity == null) {
        throw new IllegalStateException("Launch activity must be set to true or false");
      }

      return new KolibreeActivityTestRule<>(activityClass, launchActivity);
    }
  }
}

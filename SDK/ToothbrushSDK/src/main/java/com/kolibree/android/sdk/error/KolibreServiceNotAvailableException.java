package com.kolibree.android.sdk.error;

/** Created by miguelaragues on 18/9/17. */
public class KolibreServiceNotAvailableException extends Exception {

  public KolibreServiceNotAvailableException() {
    super("Kolibree Service is not bounded");
  }
}

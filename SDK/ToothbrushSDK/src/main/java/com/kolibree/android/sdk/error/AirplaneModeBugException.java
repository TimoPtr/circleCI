package com.kolibree.android.sdk.error;

/**
 * Created by aurelien on 27/04/17.
 *
 * <p>Entering then exiting airplane mode may lead to a failure reconnecting to the toothbrush on
 * some bad bluetooth stacks
 */
public final class AirplaneModeBugException extends Exception {

  public AirplaneModeBugException() {
    super("Stuck because of airplane mode bug");
  }
}

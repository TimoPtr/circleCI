package com.kolibree.bttester.tester;

/** Created by miguelaragues on 24/11/17. */
enum TestEndReason {
  PERIOD_COMPLETED(true),
  SERVICE_CONNECTION_LOST(false),
  CONNECTION_TIMED_OUT(false),
  UNKNOWN(false);

  private final boolean success;

  TestEndReason(boolean successful) {
    this.success = successful;
  }

  boolean isSuccess() {
    return success;
  }
}

/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.error;

/** Created by miguelaragues on 21/12/17. */
public class ConnectionEstablishException extends FailureReason {

  public ConnectionEstablishException(Exception exception) {
    super(exception);
  }

  public ConnectionEstablishException(String message) {
    super(message);
  }

  public ConnectionEstablishException(Throwable throwable) {
    this(new Exception(throwable));
  }
}

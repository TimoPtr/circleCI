/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.error;

/** Created by miguelaragues on 18/12/17. */
public class BluetoothNotEnabledException extends FailureReason {

  public BluetoothNotEnabledException() {
    super("Bluetooth is not enabled");
  }
}

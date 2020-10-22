/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.error;

import androidx.annotation.NonNull;

/** Created by miguelaragues on 2/4/18. */
public class DeviceNotConnectedException extends FailureReason {

  public DeviceNotConnectedException(@NonNull String message) {
    super(message);
  }
}

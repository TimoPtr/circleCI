/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.defensive

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting

@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
@VisibleForTesting
fun Preconditions.overrideEnabled(enable: Boolean) {
    Preconditions.setEnabled(enable)
}

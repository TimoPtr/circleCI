/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.stacktrace

import android.annotation.SuppressLint
import com.kolibree.android.annotation.VisibleForApp

@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
@VisibleForApp
fun printRelevantStackTrace(message: String) {
    // no-op for release
}

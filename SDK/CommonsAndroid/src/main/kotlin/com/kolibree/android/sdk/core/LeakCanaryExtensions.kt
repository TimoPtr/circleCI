/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core

import android.annotation.SuppressLint
import leakcanary.AppWatcher

@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
fun Any?.detectLeaks(name: String = "") {
    if (this != null) {
        if (AppWatcher.isInstalled) {
            AppWatcher.objectWatcher.watch(this, name.takeIf { name.isNotEmpty() } ?: "")
        }
    }
}

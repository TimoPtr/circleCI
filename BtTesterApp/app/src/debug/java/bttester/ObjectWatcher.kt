/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.bttester

import leakcanary.AppWatcher

object ObjectWatcher {

    @JvmStatic
    fun watch(any: Any, description: String) {
        AppWatcher.objectWatcher.watch(any, description)
    }
}

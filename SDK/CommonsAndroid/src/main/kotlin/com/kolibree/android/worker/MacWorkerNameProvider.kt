/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.worker

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.commons.models.StrippedMac

/**
 * This class provides the Worker tag name according to a Toothbrush Mac passed in parameter.
 */
@VisibleForApp
abstract class MacWorkerNameProvider {
    fun provide(mac: String): String {
        return "${getWorkerTag()}${StrippedMac.fromMac(mac).value}"
    }

    /**
     * Retrieve the Worker Tag which does not changes over time
     */
    abstract fun getWorkerTag(): String
}

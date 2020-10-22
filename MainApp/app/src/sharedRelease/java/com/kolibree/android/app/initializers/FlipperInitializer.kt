/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.initializers

import android.app.Application
import com.kolibree.android.app.initializers.base.AppInitializer
import javax.inject.Inject

internal class FlipperInitializer @Inject constructor() : AppInitializer {

    override fun initialize(application: Application) {
        // nop.
    }
}

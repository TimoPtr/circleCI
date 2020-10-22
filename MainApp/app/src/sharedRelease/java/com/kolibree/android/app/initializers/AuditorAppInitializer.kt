/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.initializers

import android.app.Application
import com.kolibree.android.app.initializers.base.AppInitializer
import com.kolibree.android.auditor.Auditor
import com.kolibree.android.auditor.InstabugAuditTree
import com.kolibree.android.auditor.InstabugTracker
import com.kolibree.android.extensions.appName
import javax.inject.Inject

internal class AuditorAppInitializer @Inject constructor() : AppInitializer {

    override fun initialize(application: Application) {
        with(application) {
            Auditor.instance().init(
                this,
                appName(),
                InstabugTracker(),
                InstabugAuditTree()
            )
        }
    }
}

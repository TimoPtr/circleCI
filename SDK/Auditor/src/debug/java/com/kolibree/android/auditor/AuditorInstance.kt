/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.auditor

import android.app.Application
import java.net.HttpURLConnection

internal object AuditorInstance : Auditor {

    override fun init(
        application: Application,
        tags: String,
        tracker: Tracker?,
        auditTree: AuditTree?
    ) {
        // no-op
    }

    override fun networkLog(urlConnection: HttpURLConnection) {
        // no-op
    }

    override var tracker: Tracker = NoOpTracker()
    override var auditTree: AuditTree = NoOpAuditTree()
}

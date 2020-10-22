/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.auditor

import androidx.annotation.Keep

@Keep
class NoOpAuditTree : AuditTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // no-op
    }
}

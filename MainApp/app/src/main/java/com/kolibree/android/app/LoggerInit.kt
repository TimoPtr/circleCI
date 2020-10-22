/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app

import com.kolibree.android.auditor.Auditor
import com.kolibree.android.commons.JavaLogging
import com.kolibree.android.logging.KLTimberTree
import com.kolibree.crypto.SecurityKeeper
import javax.inject.Inject
import timber.log.Timber

internal class LoggerInit @Inject constructor(
    private val securityKeeper: SecurityKeeper,
    private val kolibreeTree: KLTimberTree
) {
    fun init() {
        if (securityKeeper.isLoggingAllowed) {
            Timber.plant(kolibreeTree)

            JavaLogging.plant(kolibreeTree)
        }

        if (securityKeeper.isAuditAllowed) {
            Timber.plant(Auditor.instance().auditTree)
        }
    }
}

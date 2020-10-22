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
import com.instabug.crash.CrashReporting
import com.instabug.library.Feature
import com.instabug.library.Instabug
import com.instabug.library.invocation.InstabugInvocationEvent
import com.instabug.library.logging.InstabugNetworkLog
import com.instabug.library.ui.onboarding.WelcomeMessage
import com.instabug.library.visualusersteps.State
import java.net.HttpURLConnection

internal object AuditorInstance : Auditor {
    const val INSTABUG_TOKEN = "b8bc8a0887d67297f5190e50cc5a7a30"

    override var tracker: Tracker = NoOpTracker()

    override var auditTree: AuditTree = NoOpAuditTree()

    private val networkLogger = InstabugNetworkLog()

    override fun init(
        application: Application,
        tags: String,
        tracker: Tracker?,
        auditTree: AuditTree?
    ) {
        Instabug.Builder(application, INSTABUG_TOKEN)
            .setInvocationEvents(InstabugInvocationEvent.SHAKE)
            .setConsoleLogState(Feature.State.ENABLED)
            .setReproStepsState(State.ENABLED)
            .build()

        Instabug.setWelcomeMessageState(WelcomeMessage.State.DISABLED)

        Instabug.addTags(tags)

        CrashReporting.setState(Feature.State.DISABLED)

        tracker?.let { this.tracker = it }
        auditTree?.let { this.auditTree = it }
    }

    override fun networkLog(urlConnection: HttpURLConnection) {
        if (Instabug.isBuilt() && Instabug.isEnabled()) {
            networkLogger.log(urlConnection, null, null)
        }
    }
}

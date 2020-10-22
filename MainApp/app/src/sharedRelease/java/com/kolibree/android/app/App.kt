package com.kolibree.android.app

import com.kolibree.android.app.crashlogger.CrashLogger
import com.kolibree.android.failearly.FailEarly

/**
 * Base Application class that those build types to be released to the public (alpha, beta or
 * release) should extend
 *
 *
 * Created by miguelaragues on 18/8/17.
 */
class App : BaseKolibreeApplication() {

    override fun onCreate() {
        FailEarly.overrideDelegateWith { error, _ ->
            CrashLogger.logException(error)
        }
        super.onCreate()
    }
}

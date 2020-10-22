package com.kolibree.android.failearly.delegate

import android.annotation.SuppressLint
import com.kolibree.android.failearly.FailEarlyDelegate
import kotlin.system.exitProcess
import timber.log.Timber

/**
 * Created by lookashc on 29/01/19.
 *
 * FailEarly delegate for debug builds. Performs app death upon call.
 *
 * By separating debug and release implementations, this code will never be bundled
 * in the production app, making sure it won't be accidentally called.
 */
internal object DebugDelegate : FailEarlyDelegate {

    @SuppressLint("BinaryOperationInTimber")
    @Suppress("UNUSED_PARAMETER")
    override fun invoke(error: Throwable, fallback: () -> Unit) {
        Timber.wtf("!!!!!\n\n" +
            "======= FAILEARLY KICKED IN =======\n" +
            "=                                 =\n" +
            "=    This is a dev build.         =\n" +
            "=    All critical errors are      =\n" +
            "=    causing app restart.         =\n" +
            "=                                 =\n" +
            "=    Please check your earlier    =\n" +
            "=    logcat output to find out    =\n" +
            "=    why this happened.           =\n" +
            "=                                 =\n" +
            "=            [*][*][*]            =\n" +
            "=                                 =\n" +
            "===================================\n\n"
        )
        exitProcess(1)
    }
}

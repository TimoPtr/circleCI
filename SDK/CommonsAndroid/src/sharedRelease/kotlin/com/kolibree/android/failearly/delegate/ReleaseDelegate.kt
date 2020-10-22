package com.kolibree.android.failearly.delegate

import com.kolibree.android.failearly.FailEarlyDelegate

/**
 * Created by lookashc on 13/02/19.
 *
 * FailEarly extension for release builds.
 *
 * In release builds we don't want the app to die - let's try to recover it.
 */
internal object ReleaseDelegate : FailEarlyDelegate {

    override fun invoke(error: Throwable, fallback: () -> Unit) {
        fallback()
    }
}

package com.kolibree.android.test.utils.failearly.delegate

import androidx.annotation.VisibleForTesting
import com.kolibree.android.failearly.FailEarlyDelegate
import timber.log.Timber

/**
 * Created by lookashc on 13/02/19.
 */
@VisibleForTesting
object FallbackTestDelegate : FailEarlyDelegate {

    override fun invoke(error: Throwable, fallback: () -> Unit) {
        Timber.w("FAILEARLY KICKED IN, calling fallback")
        fallback()
    }
}

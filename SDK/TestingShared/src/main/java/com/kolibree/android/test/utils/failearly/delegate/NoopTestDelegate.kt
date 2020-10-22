package com.kolibree.android.test.utils.failearly.delegate

import androidx.annotation.VisibleForTesting
import com.kolibree.android.failearly.FailEarlyDelegate
import timber.log.Timber

/**
 * Created by lookashc on 13/02/19.
 *
 * FailEarly delegate for tests which may fail because of fail checks.
 * Should be used only as a last resort solution.
 */
@VisibleForTesting
object NoopTestDelegate : FailEarlyDelegate {

    override fun invoke(throwable: Throwable, fallback: () -> Unit) {
        Timber.w("FAILEARLY KICKED IN, no action as this is no-op delegate")
    }
}

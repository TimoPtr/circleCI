package com.kolibree.android.failearly

import com.kolibree.android.failearly.delegate.DebugDelegate
import timber.log.Timber

/**
 * Created by lookashc on 13/02/19.
 */
internal fun FailEarly.setupDefaultDelegate() {
    Timber.d("Setting FailEarly delegate to DebugDelegate")
    delegate = DebugDelegate
}

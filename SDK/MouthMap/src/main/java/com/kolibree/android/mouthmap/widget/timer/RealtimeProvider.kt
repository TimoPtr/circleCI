/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.mouthmap.widget.timer

import android.os.SystemClock
import androidx.annotation.Keep
import javax.inject.Inject

@Keep
interface RealtimeProvider {
    fun elapsedRealtime(): Long
}

internal class RealtimeProviderImpl @Inject constructor() :
    RealtimeProvider {
    override fun elapsedRealtime() = SystemClock.elapsedRealtime()
}

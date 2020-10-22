/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushing

import androidx.annotation.Keep
import io.reactivex.Flowable

@Keep
interface BrushingSessionMonitor {

    /**
     * This stream emits the brushing session status on main thread and does not complete, does not emit
     * any errors
     *
     * true: session started
     * false: session stopped
     */
    val sessionMonitorStream: Flowable<Boolean>
}

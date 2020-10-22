/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sba.testbrushing.brushing.creator

import androidx.lifecycle.DefaultLifecycleObserver
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.processedbrushings.CheckupData
import com.kolibree.android.sdk.connection.KLTBConnection

@VisibleForApp
interface TestBrushingCreator : DefaultLifecycleObserver {
    fun start(connection: KLTBConnection)
    fun create(connection: KLTBConnection): CheckupData
    fun pause(connection: KLTBConnection)
    fun resume(connection: KLTBConnection)
    fun notifyReconnection()
}

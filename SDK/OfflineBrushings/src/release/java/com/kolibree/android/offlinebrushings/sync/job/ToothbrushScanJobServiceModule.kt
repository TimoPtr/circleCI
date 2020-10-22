/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.sync.job

import dagger.Module
import dagger.Provides
import org.threeten.bp.Duration

@Module
internal object ToothbrushScanJobServiceModule {
    private const val MIN_DELAY_MINUTES = 30L

    private val delayReleaseVersion = Duration.ofMinutes(MIN_DELAY_MINUTES)

    @Provides
    @ToothbrushScanLatency
    fun providesDebugToothbrushScanLatency(): Duration = delayReleaseVersion
}

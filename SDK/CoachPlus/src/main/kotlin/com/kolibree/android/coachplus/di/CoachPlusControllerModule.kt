/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.di

import com.kolibree.android.processedbrushings.ProcessedBrushingsModule
import com.kolibree.android.sdk.util.KpiSpeedProviderModule
import com.kolibree.android.sdk.util.RnnWeightProviderModule
import dagger.Module

@Module(
    includes = [
        CoachPlusControllerInternalModule::class,
        RnnWeightProviderModule::class,
        KpiSpeedProviderModule::class,
        ProcessedBrushingsModule::class
    ]
)
object CoachPlusControllerModule

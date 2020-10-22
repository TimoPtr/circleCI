/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.util

import android.content.Context
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.crypto.KolibreeGuard
import dagger.Module
import dagger.Provides

@Module
object KpiSpeedProviderModule {
    @Provides
    fun providesKpiSpeed(
        context: Context,
        kolibreeGuard: KolibreeGuard,
        toothbrushModel: ToothbrushModel?
    ): KpiSpeedProvider? =
        toothbrushModel?.let { KpiSpeedProviderImpl(context.applicationContext, kolibreeGuard, toothbrushModel) }
}

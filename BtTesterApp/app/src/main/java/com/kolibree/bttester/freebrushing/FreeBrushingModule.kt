/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.bttester.freebrushing

import android.content.Context
import com.kolibree.android.utils.KolibreeAppVersions
import dagger.Module
import dagger.Provides

@Module
internal class FreeBrushingModule {

    @Provides
    fun providesAppVersions(context: Context): KolibreeAppVersions = KolibreeAppVersions(context)
}

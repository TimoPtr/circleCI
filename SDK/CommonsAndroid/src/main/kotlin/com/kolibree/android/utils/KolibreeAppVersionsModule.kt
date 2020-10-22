/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.utils

import com.kolibree.android.app.dagger.ApplicationContext
import dagger.Module
import dagger.Provides

@Module
object KolibreeAppVersionsModule {

    @Provides
    fun providesAppVersions(context: ApplicationContext): KolibreeAppVersions =
        KolibreeAppVersions(context)
}

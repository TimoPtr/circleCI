/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.appversion.di

import com.kolibree.android.appversion.AppVersionProvider
import com.kolibree.android.appversion.AppVersionProviderImpl
import dagger.Binds
import dagger.Module

@Module
internal abstract class AppVersionModule {
    @Binds
    abstract fun bindsAppVersionProvider(impl: AppVersionProviderImpl): AppVersionProvider
}

/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.welcome

import android.content.Context
import dagger.Module
import dagger.Provides
import java.lang.ref.WeakReference

@Module
object InstallationSourceModule {

    @Provides
    fun provideInstallationSource(context: Context): InstallationSource {
        return if (CreateAccountUtils.installedFromMarket(WeakReference(context)))
            InstallationSource.GOOGLE_PLAY else InstallationSource.UNKNOWN
    }
}

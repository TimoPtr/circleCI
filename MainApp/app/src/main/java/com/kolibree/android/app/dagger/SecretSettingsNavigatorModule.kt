/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger

import androidx.appcompat.app.AppCompatActivity
import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import com.kolibree.android.app.ui.settings.secret.SecretSettingsActivity
import com.kolibree.android.app.ui.settings.secret.SecretSettingsNavigator
import com.kolibree.android.app.ui.settings.secret.SecretSettingsNavigatorImpl
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
internal interface SecretSettingsNavigatorModule {

    @Binds
    fun bindAppCompatActivity(
        implementation: SecretSettingsActivity
    ): AppCompatActivity

    companion object {

        @Provides
        fun providesNavigator(activity: SecretSettingsActivity): SecretSettingsNavigator {
            return activity.createNavigatorAndBindToLifecycle(SecretSettingsNavigatorImpl::class)
        }
    }
}

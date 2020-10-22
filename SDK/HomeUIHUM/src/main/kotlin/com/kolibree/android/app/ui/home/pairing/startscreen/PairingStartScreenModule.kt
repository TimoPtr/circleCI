/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.pairing.startscreen

import androidx.appcompat.app.AppCompatActivity
import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
internal abstract class PairingStartScreenModule {

    @Binds
    abstract fun bindAppCompatActivity(activity: PairingStartScreenActivity): AppCompatActivity

    internal companion object {

        @Provides
        fun providesNavigator(activity: PairingStartScreenActivity): PairingStartScreenNavigator {
            return activity.createNavigatorAndBindToLifecycle(PairingStartScreenNavigator::class)
        }
    }
}

/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.pairing

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.kolibree.android.app.ui.pairing.PairingFlowHost
import com.kolibree.android.app.ui.pairing.PairingFlowModule
import com.kolibree.pairing.PairingModule
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(
    includes = [
        PairingModule::class,
        PairingFlowModule::class
    ]
)
internal abstract class ToothbrushPairingModule {

    @Binds
    abstract fun bindAppCompatActivity(activity: ToothbrushPairingActivity): AppCompatActivity

    internal companion object {

        @Provides
        internal fun provideActivityViewModel(
            activity: ToothbrushPairingActivity,
            viewModelFactory: ToothbrushPairingViewModel.Factory
        ): ToothbrushPairingViewModel {
            return ViewModelProvider(
                activity,
                viewModelFactory
            ).get(ToothbrushPairingViewModel::class.java)
        }
    }

    @Binds
    internal abstract fun bindsPairingHost(toothbrushPairingViewModel: ToothbrushPairingViewModel): PairingFlowHost
}

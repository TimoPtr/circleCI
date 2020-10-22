/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */
package com.kolibree.bttester.ota.di

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.ui.ota.feature.AlwaysOfferOtaUpdateModule
import com.kolibree.android.toothbrushupdate.CheckOtaUpdatePrerequisitesModule
import com.kolibree.bttester.legacy.PairDialogFragmentModule
import com.kolibree.bttester.ota.logic.OtaPersistentState
import com.kolibree.bttester.ota.mvi.OtaActivity
import dagger.Module
import dagger.Provides

@Module(includes = [PairDialogFragmentModule::class, AlwaysOfferOtaUpdateModule::class, CheckOtaUpdatePrerequisitesModule::class])
class OtaActivityModule {
    @Provides
    internal fun provideLifecycle(activity: OtaActivity): Lifecycle = activity.lifecycle

    @Provides
    internal fun provideOtaPersistentState(context: Context): OtaPersistentState =
        OtaPersistentState(context)
}
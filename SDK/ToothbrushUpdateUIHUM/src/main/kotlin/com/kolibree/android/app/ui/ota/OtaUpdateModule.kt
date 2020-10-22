/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.ota

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import com.kolibree.android.app.dagger.scopes.FragmentScope
import com.kolibree.android.app.ui.ota.inprogress.InProgressOtaFragment
import com.kolibree.android.app.ui.ota.start.StartOtaFragment
import com.kolibree.android.toothbrushupdate.CheckOtaUpdatePrerequisitesModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module(includes = [CheckOtaUpdatePrerequisitesModule::class])
internal abstract class OtaUpdateModule {

    @Binds
    abstract fun bindAppCompatActivity(activity: OtaUpdateActivity): AppCompatActivity

    internal companion object {
        @Provides
        internal fun provideSharedViewModel(
            activity: OtaUpdateActivity,
            viewModelFactory: OtaUpdateViewModel.Factory
        ): OtaUpdateSharedViewModel = ViewModelProvider(
            activity,
            viewModelFactory
        ).get(OtaUpdateViewModel::class.java)

        @Provides
        fun providesNavigator(activity: OtaUpdateActivity): OtaUpdateNavigator {
            return activity.createNavigatorAndBindToLifecycle(OtaUpdateNavigator::class)
        }

        @Provides
        fun providesOtaUpdateParams(activity: OtaUpdateActivity): OtaUpdateParams = activity.params
    }

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeStartOtaFragment(): StartOtaFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeInProgressOtaFragment(): InProgressOtaFragment
}

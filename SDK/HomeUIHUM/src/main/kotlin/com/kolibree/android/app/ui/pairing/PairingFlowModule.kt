/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing

import androidx.appcompat.app.AppCompatActivity
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import com.kolibree.android.app.base.createViewModelAndBindToLifeCycle
import com.kolibree.android.app.dagger.scopes.FragmentScope
import com.kolibree.android.app.ui.pairing.brush_found.BrushFoundFragment
import com.kolibree.android.app.ui.pairing.enablebluetooth.EnableBluetoothFragment
import com.kolibree.android.app.ui.pairing.enablebluetooth.di.EnableBluetoothFragmentModule
import com.kolibree.android.app.ui.pairing.is_brush_ready.IsBrushReadyFragment
import com.kolibree.android.app.ui.pairing.list.ScanToothbrushListFragment
import com.kolibree.android.app.ui.pairing.location.LocationFragment
import com.kolibree.android.app.ui.pairing.location.di.LocationFragmentModule
import com.kolibree.android.app.ui.pairing.model_mismatch.ModelMismatchFragment
import com.kolibree.android.app.ui.pairing.wake_your_brush.WakeYourBrushFragment
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
@VisibleForApp
abstract class PairingFlowModule {

    internal companion object {

        @Provides
        internal fun providePairingViewModel(
            activity: AppCompatActivity,
            viewModelFactory: PairingViewModel.Factory
        ): PairingViewModel {
            return activity.createViewModelAndBindToLifeCycle<PairingViewModel> { viewModelFactory }
        }

        @Provides
        fun providesNavigator(activity: AppCompatActivity): PairingNavigator {
            return activity.createNavigatorAndBindToLifecycle(PairingNavigator::class)
        }
    }

    @Binds
    internal abstract fun bindPairingFlowSharedFacade(impl: PairingFlowSharedFacadeImpl): PairingFlowSharedFacade

    @Binds
    internal abstract fun bindPairingSharedViewModel(impl: PairingViewModel): PairingSharedViewModel

    @Binds
    internal abstract fun bindBlinkingConnectionHolder(impl: BlinkingConnectionHolderImpl): BlinkingConnectionHolder

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun contributeWakeYourBrushFragment(): WakeYourBrushFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun contributeBrushFoundFragment(): BrushFoundFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [EnableBluetoothFragmentModule::class])
    internal abstract fun contributeEnableBluetoothFragment(): EnableBluetoothFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [LocationFragmentModule::class])
    internal abstract fun contributeLocationFragment(): LocationFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun contributeIsBrushReadyFragment(): IsBrushReadyFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun contributeModelMismatchFragment(): ModelMismatchFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun contributeScanToothbrushListFragment(): ScanToothbrushListFragment
}

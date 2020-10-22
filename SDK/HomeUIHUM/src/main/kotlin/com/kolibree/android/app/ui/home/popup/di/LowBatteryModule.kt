/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.popup.di

import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.base.createAndBindToLifecycle
import com.kolibree.android.app.ui.home.popup.lowbattery.LowBatteryProvider
import com.kolibree.android.app.ui.home.popup.lowbattery.LowBatteryProviderImpl
import com.kolibree.android.app.ui.home.popup.lowbattery.LowBatteryUseCase
import com.kolibree.android.app.ui.home.popup.lowbattery.LowBatteryUseCaseImpl
import com.kolibree.android.app.ui.home.popup.lowbattery.LowBatteryViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
internal abstract class LowBatteryModule {

    @Binds
    abstract fun bindLowBatteryUseCase(impl: LowBatteryUseCaseImpl): LowBatteryUseCase

    @Binds
    abstract fun bindLowBatteryProvider(impl: LowBatteryProviderImpl): LowBatteryProvider

    companion object {
        @Provides
        fun provideTestBrushingPopupViewModel(
            activity: BaseMVIActivity<*, *, *, *, *>,
            viewModelFactory: LowBatteryViewModel.Factory
        ): LowBatteryViewModel =
            viewModelFactory.createAndBindToLifecycle(activity, LowBatteryViewModel::class.java)
    }
}

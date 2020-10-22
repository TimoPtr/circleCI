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
import com.kolibree.android.app.ui.home.popup.testbrushing.TestBrushingPriorityDisplayViewModel
import dagger.Module
import dagger.Provides

@Module
internal class TestBrushingPriorityDisplayModule {
    @Provides
    fun provideTestBrushingPopupViewModel(
        activity: BaseMVIActivity<*, *, *, *, *>,
        viewModelFactory: TestBrushingPriorityDisplayViewModel.Factory
    ): TestBrushingPriorityDisplayViewModel =
        viewModelFactory.createAndBindToLifecycle(activity, TestBrushingPriorityDisplayViewModel::class.java)
}

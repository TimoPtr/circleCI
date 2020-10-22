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
import com.kolibree.android.app.ui.home.popup.offline.OfflineBrushingRetrievalViewModel
import dagger.Module
import dagger.Provides

@Module
internal object OfflineBrushingRetrievalModule {

    @Provides
    fun provideOfflineBrushingRetrievalModuleViewModel(
        activity: BaseMVIActivity<*, *, *, *, *>,
        viewModelFactory: OfflineBrushingRetrievalViewModel.Factory
    ): OfflineBrushingRetrievalViewModel =
        viewModelFactory.createAndBindToLifecycle(
            activity,
            OfflineBrushingRetrievalViewModel::class.java
        )
}

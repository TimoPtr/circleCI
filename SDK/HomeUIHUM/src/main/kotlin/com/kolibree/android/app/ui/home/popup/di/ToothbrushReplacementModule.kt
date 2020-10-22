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
import com.kolibree.android.app.ui.home.popup.tbreplace.HeadReplacementProvider
import com.kolibree.android.app.ui.home.popup.tbreplace.HeadReplacementProviderImpl
import com.kolibree.android.app.ui.home.popup.tbreplace.HeadReplacementUseCase
import com.kolibree.android.app.ui.home.popup.tbreplace.HeadReplacementUseCaseImpl
import com.kolibree.android.app.ui.home.popup.tbreplace.HeadReplacementViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
internal abstract class ToothbrushReplacementModule {

    @Binds
    abstract fun bindToothbrushReplaceUseCase(impl: HeadReplacementUseCaseImpl):
        HeadReplacementUseCase

    @Binds
    abstract fun bindHeadReplacementProvider(impl: HeadReplacementProviderImpl):
        HeadReplacementProvider

    companion object {
        @Provides
        fun provideTestBrushingPopupViewModel(
            activity: BaseMVIActivity<*, *, *, *, *>,
            viewModelFactory: HeadReplacementViewModel.Factory
        ): HeadReplacementViewModel =
            viewModelFactory.createAndBindToLifecycle(activity, HeadReplacementViewModel::class.java)
    }
}

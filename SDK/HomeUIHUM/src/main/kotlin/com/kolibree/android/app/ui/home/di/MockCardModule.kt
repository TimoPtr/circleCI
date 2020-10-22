/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.di

import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.base.createAndBindToLifecycle
import com.kolibree.android.app.ui.card.DynamicCardViewModel
import com.kolibree.android.app.ui.card.mock.MockCardViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

/**
 * Remove when it will become obsolete
 */
@Module
internal object MockCardModule {

    @Provides
    @IntoSet
    fun provideMockCardFragment(
        fragment: BaseMVIFragment<*, *, *, *, *>,
        viewModelFactory: MockCardViewModel.Factory
    ): DynamicCardViewModel<*, *, *> =
        viewModelFactory.createAndBindToLifecycle(fragment, MockCardViewModel::class.java)
}

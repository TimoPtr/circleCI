/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.trial.card.di

import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.base.createAndBindToLifecycle
import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import com.kolibree.android.app.ui.card.DynamicCardViewModel
import com.kolibree.android.headspace.trial.card.HeadspaceTrialCardViewModel
import com.kolibree.android.headspace.trial.card.HeadspaceTrialNavigator
import com.kolibree.android.headspace.trial.card.HeadspaceTrialNavigatorFactory
import com.kolibree.android.headspace.trial.card.HeadspaceTrialNavigatorImpl
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Module
object HeadspaceTrialCardModule {

    @Provides
    @IntoSet
    internal fun provideHeadspaceTrialCardViewModel(
        fragment: BaseMVIFragment<*, *, *, *, *>,
        viewModelFactory: HeadspaceTrialCardViewModel.Factory
    ): DynamicCardViewModel<*, *, *> =
        viewModelFactory.createAndBindToLifecycle(
            fragment,
            HeadspaceTrialCardViewModel::class.java
        )

    @Provides
    internal fun providesHeadspaceTrialNavigator(
        fragment: BaseMVIFragment<*, *, *, *, *>,
        factory: HeadspaceTrialNavigatorFactory
    ): HeadspaceTrialNavigator {
        return fragment.createNavigatorAndBindToLifecycle(HeadspaceTrialNavigatorImpl::class) { factory }
    }
}

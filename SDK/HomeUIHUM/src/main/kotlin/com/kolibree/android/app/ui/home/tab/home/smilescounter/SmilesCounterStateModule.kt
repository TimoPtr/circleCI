/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.smilescounter

import com.kolibree.android.app.dagger.scopes.FragmentScope
import dagger.Binds
import dagger.Module

@Module
internal abstract class SmilesCounterStateModule {

    @Binds
    @FragmentScope
    abstract fun bindsSmilesCounterStateProvider(impl: SmilesCounterStateProviderImpl): SmilesCounterStateProvider

    @Binds
    abstract fun bindsSmilesCounterStateMerger(impl: SmilesCounterStateMergerImpl): SmilesCounterStateMerger
}

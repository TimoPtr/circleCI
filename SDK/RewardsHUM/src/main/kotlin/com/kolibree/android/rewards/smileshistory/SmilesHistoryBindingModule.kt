/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.smileshistory

import com.kolibree.android.app.dagger.scopes.ActivityScope
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SmilesHistoryBindingModule {

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun bindSmilesHistoryActivity(): SmilesHistoryActivity
}

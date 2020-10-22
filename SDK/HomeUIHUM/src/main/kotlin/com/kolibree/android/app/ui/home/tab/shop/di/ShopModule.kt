/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.shop.di

import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.ui.home.tab.shop.ShopFragment
import dagger.Binds
import dagger.Module

@Module
internal abstract class ShopModule {

    @Binds
    abstract fun bindMviFragment(
        implementation: ShopFragment
    ): BaseMVIFragment<*, *, *, *, *>
}

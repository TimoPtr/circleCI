/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.databinding.di

import com.kolibree.android.app.ui.databinding.avatar.PicassoAvatarBindingAdapter
import com.kolibree.databinding.bindingadapter.AvatarBindingAdapter
import dagger.Module
import dagger.Provides

@Module
object DataBindingAdapterModule {

    @Provides
    fun provideAvatarBindingAdapter(): AvatarBindingAdapter {
        return PicassoAvatarBindingAdapter()
    }
}

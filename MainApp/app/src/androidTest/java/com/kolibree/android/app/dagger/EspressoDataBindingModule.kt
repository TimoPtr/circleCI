/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger

import com.kolibree.android.app.ui.databinding.avatar.TextOnlyAvatarBindingAdapter
import com.kolibree.databinding.bindingadapter.AvatarBindingAdapter
import dagger.Binds
import dagger.Module

@Module
abstract class EspressoDataBindingModule {

    @Binds
    abstract fun provideAvatarBindingAdapter(impl: TextOnlyAvatarBindingAdapter): AvatarBindingAdapter
}

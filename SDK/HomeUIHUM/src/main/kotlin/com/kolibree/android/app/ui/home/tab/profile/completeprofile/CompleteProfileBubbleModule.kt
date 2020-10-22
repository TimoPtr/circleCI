/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.profile.completeprofile

import dagger.Binds
import dagger.Module

@Module
abstract class CompleteProfileBubbleModule {

    @Binds
    internal abstract fun bindCompleteProfileBubbleUseCase(
        impl: CompleteProfileBubbleUseCaseImpl
    ): CompleteProfileBubbleUseCase
}

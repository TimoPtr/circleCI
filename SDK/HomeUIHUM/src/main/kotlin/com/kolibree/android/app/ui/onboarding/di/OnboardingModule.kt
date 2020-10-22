/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding.di

import androidx.annotation.Keep
import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.app.ui.onboarding.OnboardingActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Keep
@Module
abstract class OnboardingModule {

    @ActivityScope
    @ContributesAndroidInjector(
        modules = [
            OnboardingActivityModule::class
        ]
    )
    internal abstract fun bindOnboardingActivity(): OnboardingActivity
}

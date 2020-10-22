/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger

import com.kolibree.android.app.ui.onboarding.di.OnboardingActivityBindingModule
import com.kolibree.android.app.ui.onboarding.di.OnboardingFragmentModule
import com.kolibree.android.app.ui.pairing.PairingFlowModule
import com.kolibree.android.app.ui.welcome.InstallationSourceModule
import dagger.Module

@Module(
    includes = [
        OnboardingActivityBindingModule::class,
        OnboardingFragmentModule::class,
        EspressoGoogleSignInModule::class,
        InstallationSourceModule::class,
        PairingFlowModule::class
    ]
)
abstract class EspressoOnboardingActivityModule

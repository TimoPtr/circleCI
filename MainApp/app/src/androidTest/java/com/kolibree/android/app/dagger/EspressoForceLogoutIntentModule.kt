/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger

import android.content.Context
import com.kolibree.account.logout.IntentAfterForcedLogout
import com.kolibree.account.logout.IntentAfterForcedLogout.Companion.create
import com.kolibree.android.app.ui.onboarding.OnboardingActivity
import dagger.Module
import dagger.Provides

@Module
class EspressoForceLogoutIntentModule {

    @Provides
    fun providesAfterLogoutIntent(context: Context): IntentAfterForcedLogout {
        return create(context, OnboardingActivity::class.java)
    }
}

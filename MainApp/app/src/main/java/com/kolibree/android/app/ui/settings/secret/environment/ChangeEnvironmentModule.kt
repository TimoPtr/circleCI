/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.secret.environment

import com.kolibree.android.app.base.createAndBindToLifecycle
import com.kolibree.android.app.ui.settings.secret.SecretSettingsActivity
import dagger.Module
import dagger.Provides

@Module
class ChangeEnvironmentModule {

    @Provides
    internal fun providesChangeEnvironmentViewModel(
        activity: SecretSettingsActivity,
        viewModelFactory: ChangeEnvironmentViewModel.Factory
    ): ChangeEnvironmentViewModel =
        viewModelFactory.createAndBindToLifecycle(activity, ChangeEnvironmentViewModel::class.java)
}

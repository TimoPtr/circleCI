/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.secret

import android.content.Context
import android.content.Intent
import com.kolibree.android.failearly.FailEarly
import dagger.Binds
import dagger.Module
import javax.inject.Inject

@Module
abstract class SecretSettingsFactoryModule {
    @Binds
    internal abstract fun bindsSecretSettingsFactory(
        implementation: SecretSettingsFactoryImpl
    ): SecretSettingsFactory
}

internal class SecretSettingsFactoryImpl @Inject constructor() : SecretSettingsFactory {
    override fun secretSettingsIntent(context: Context): Intent {
        return Intent(context, SecretSettingsActivity::class.java)
    }

    override fun legacySecretSettingsIntent(context: Context): Intent {
        FailEarly.fail("Hum doesn't support legacy secret settings")
        return Intent()
    }
}

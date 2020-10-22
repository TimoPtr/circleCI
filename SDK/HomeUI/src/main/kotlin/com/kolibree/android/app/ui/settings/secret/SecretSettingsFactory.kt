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
import com.kolibree.android.annotation.VisibleForApp

@VisibleForApp
interface SecretSettingsFactory {
    fun secretSettingsIntent(context: Context): Intent
    fun legacySecretSettingsIntent(context: Context): Intent
}

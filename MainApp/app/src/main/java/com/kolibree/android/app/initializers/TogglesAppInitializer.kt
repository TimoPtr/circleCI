/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.initializers

import android.app.Application
import com.kolibree.android.app.initializers.base.AppInitializer
import com.kolibree.android.app.ui.settings.secret.persistence.InstallationFlags
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.feature.OfflineBrushingsNotificationsFeature
import com.kolibree.android.feature.toggleForFeature
import javax.inject.Inject

internal class TogglesAppInitializer @Inject constructor(
    private val featuresToggle: FeatureToggleSet,
    private val installationFlags: InstallationFlags
) : AppInitializer {

    override fun initialize(application: Application) {
        installationFlags.handleFlagIfNeeded(InstallationFlags.Flag.SHOULD_CONFIGURE_FEATURE_TOGGLES) {
            featuresToggle.toggleForFeature(OfflineBrushingsNotificationsFeature).value = true
        }
    }
}

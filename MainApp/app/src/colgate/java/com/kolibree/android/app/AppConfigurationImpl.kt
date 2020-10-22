/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app

import com.kolibree.android.commons.AppConfiguration

internal object AppConfigurationImpl : AppConfiguration {
    override val showPromotionsOptionAtSignUp: Boolean = true
    override val allowDisablingDataSharing: Boolean = true
    override val isSelectProfileSupported: Boolean = true
    override val isBatteryMonitoringEnabled: Boolean = false
    override val isMultiToothbrushesPerProfileEnabled: Boolean = false
    override val showHeadspaceRelatedContent: Boolean = false
    override val showGuidedBrushingTips: Boolean = false
    override val showGamesCard: Boolean = true
}

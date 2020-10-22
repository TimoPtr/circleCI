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
    override val showPromotionsOptionAtSignUp: Boolean = false
    override val allowDisablingDataSharing: Boolean = false
    override val isSelectProfileSupported: Boolean = false
    override val isBatteryMonitoringEnabled: Boolean = true
    override val isMultiToothbrushesPerProfileEnabled: Boolean = false
    override val showHeadspaceRelatedContent: Boolean = true
    override val showGuidedBrushingTips: Boolean = true
    override val showGamesCard: Boolean = false
}

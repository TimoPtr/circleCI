/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.feature

import androidx.annotation.Keep

@Keep
object GooglePayProductionEnvironmentFeature : Feature<Boolean> {

    override val initialValue = false

    override val displayable = true

    override val displayName = "Use Google Pay Production"

    override val requiresAppRestart = false
}

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
object UseTestShopFeature : Feature<Boolean> {

    override val initialValue: Boolean
        get() = false

    override val displayable: Boolean
        get() = true

    override val displayName: String
        get() = "Use Test Shop"

    override val requiresAppRestart: Boolean
        get() = true
}

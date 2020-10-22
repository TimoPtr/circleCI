/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.feature

import androidx.annotation.Keep

@Keep
object ShowOrphanBrushingsInCheckupFeature : Feature<Boolean> {

    override val initialValue: Boolean
        get() = false

    override val displayable: Boolean
        get() = false

    override val displayName: String
        get() = "Show Orphan Brushings In Checkup"

    override val requiresAppRestart: Boolean
        get() = true
}

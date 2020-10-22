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
object UseWideContextualMessageFeature : Feature<Boolean> {

    override val initialValue: Boolean
        get() = true

    override val displayable: Boolean
        get() = false

    override val displayName: String
        get() = "[UI 2.0] Wide Contextual Message"

    override val requiresAppRestart: Boolean
        get() = false
}

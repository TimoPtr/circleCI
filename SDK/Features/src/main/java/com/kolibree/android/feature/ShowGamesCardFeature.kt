/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.feature

object ShowGamesCardFeature : Feature<Boolean> {

    override val initialValue: Boolean = false

    override val displayable: Boolean = true

    override val displayName: String = "Show Games card"

    override val requiresAppRestart: Boolean = true
}

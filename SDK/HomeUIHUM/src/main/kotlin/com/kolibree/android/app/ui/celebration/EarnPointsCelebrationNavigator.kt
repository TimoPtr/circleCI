/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.celebration

import com.kolibree.android.app.base.BaseNavigator

internal class EarnPointsCelebrationNavigator : BaseNavigator<EarnPointsCelebrationActivity>() {

    fun finish() = withOwner { finish() }
}

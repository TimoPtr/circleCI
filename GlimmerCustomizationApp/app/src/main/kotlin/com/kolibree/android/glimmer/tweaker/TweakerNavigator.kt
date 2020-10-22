/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker

import com.kolibree.android.app.base.BaseNavigator
import com.kolibree.android.glimmer.pairing.startPairingIntent

internal class TweakerNavigator : BaseNavigator<TweakerActivity>() {

    fun navigateToPairingActivity() = withOwner {
        startPairingIntent(this)
        finish()
    }
}

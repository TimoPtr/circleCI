/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.smilescounter

import com.kolibree.android.homeui.hum.R
import com.kolibree.databinding.bindingadapter.LottieDelayedLoop

internal val smilesBackgroundLaunchAnimation = LottieDelayedLoop(
    rawRes = R.raw.start_animation,
    loopStartFrame = 510,
    loopEndFrame = 570
)

internal val smilesBackgroundIncreaseAnimation = LottieDelayedLoop(
    rawRes = R.raw.smile_point_anim,
    loopStartFrame = 479,
    loopEndFrame = 571
)

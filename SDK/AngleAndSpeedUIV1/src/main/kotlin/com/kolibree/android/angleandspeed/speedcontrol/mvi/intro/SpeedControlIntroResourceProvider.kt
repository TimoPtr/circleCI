/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.speedcontrol.mvi.intro

import com.kolibree.android.angleandspeed.R
import com.kolibree.android.app.mvi.intro.GameIntroResourceProvider

internal object SpeedControlIntroResourceProvider : GameIntroResourceProvider {

    override fun headerTextResId() = R.string.speed_control_intro_header

    override fun bodyTextResId() = R.string.speed_control_intro_body

    override fun animatedGifResId() = R.raw.animated_gif_speed

    override fun startButtonTextResId() = R.string.speed_control_intro_start_button
}

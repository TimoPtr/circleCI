/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.testangles.mvi.intro

import com.kolibree.android.angleandspeed.R
import com.kolibree.android.app.mvi.intro.GameIntroResourceProvider

internal object TestAnglesIntroResourceProvider : GameIntroResourceProvider {

    override fun headerTextResId() = R.string.test_angles_intro_header

    override fun bodyTextResId() = R.string.test_angles_intro_body

    override fun animatedGifResId() = R.raw.animated_gif_molars

    override fun startButtonTextResId() = R.string.test_angles_intro_start_button
}

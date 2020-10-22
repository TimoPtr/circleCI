/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.game

import android.annotation.SuppressLint

@SuppressLint("DeobfuscatedPublicSdkClass")
enum class ActivityGame(
    val isUnityGame: Boolean = false,
    val hasManualMode: Boolean = false
) {

    Pirate(isUnityGame = true),
    Rabbids(isUnityGame = true),
    Archaelogy(isUnityGame = true),
    Coach(hasManualMode = true),
    CoachPlus(hasManualMode = true),
    TestBrushing,
    TestAngles,
    SpeedControl;
}

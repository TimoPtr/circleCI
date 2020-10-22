/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.activities

import com.kolibree.android.app.ui.game.ActivityGame

internal interface ActivitiesInteraction :
    CoachedBrushingInteraction,
    BrushTimerInteraction,
    ActivityInteraction,
    GameInteraction

internal interface CoachedBrushingInteraction {
    fun onCoachedBrushingCardClick()
}

internal interface BrushTimerInteraction {
    fun onBrushTimerCardClick()
}

internal interface ActivityInteraction {
    fun onActivityClick(game: ActivityGame)
}

internal interface GameInteraction {
    fun onGameClick(game: ActivityGame)
}

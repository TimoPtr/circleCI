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
import com.kolibree.android.app.ui.navigation.HomeScreenAction

internal sealed class ActivitiesActions : HomeScreenAction

internal data class StartGame(val game: ActivityGame) : ActivitiesActions()

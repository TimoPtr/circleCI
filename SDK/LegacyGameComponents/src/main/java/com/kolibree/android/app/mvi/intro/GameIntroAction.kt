/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.mvi.intro

import androidx.annotation.Keep
import com.kolibree.android.game.mvi.BaseGameAction

@Keep
sealed class GameIntroAction : BaseGameAction

internal object OpenBrushScreen : GameIntroAction()

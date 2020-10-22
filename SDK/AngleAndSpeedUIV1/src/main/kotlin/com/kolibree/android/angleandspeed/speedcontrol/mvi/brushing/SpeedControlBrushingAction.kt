/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.speedcontrol.mvi.brushing

import com.kolibree.android.game.mvi.BaseGameAction

internal sealed class SpeedControlBrushingAction : BaseGameAction

internal object OpenConfirmation : SpeedControlBrushingAction()

/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.mvi

import androidx.annotation.Keep
import com.kolibree.android.app.base.BaseAction
import com.kolibree.android.sdk.disconnection.LostConnectionHandler

@Keep
interface BaseGameAction : BaseAction

@Keep
data class VibratorStateChanged(val isOn: Boolean) : BaseGameAction

@Keep
data class ConnectionHandlerStateChanged(
    val state: LostConnectionHandler.State
) : BaseGameAction

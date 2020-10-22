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
import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.sdk.disconnection.LostConnectionHandler

@Keep
interface BaseGameViewState : BaseViewState {

    val lostConnectionState: LostConnectionHandler.State?
}

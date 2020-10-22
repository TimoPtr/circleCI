/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.pirate

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.game.GameViewState
import com.kolibree.android.game.GameViewState.ActionId

/**
 * Created by miguelaragues on 17/10/17.
 */

@VisibleForApp
data class PirateFragmentViewState(@ActionId override val actionId: Int) :
    GameViewState<PirateFragmentViewState> {
    override fun withActionId(actionId: Int): PirateFragmentViewState {
        return PirateFragmentViewState(actionId)
    }
}

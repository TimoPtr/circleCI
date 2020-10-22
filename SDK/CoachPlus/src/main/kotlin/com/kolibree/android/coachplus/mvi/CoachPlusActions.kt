/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.mvi

import androidx.annotation.Keep
import com.kolibree.android.game.mvi.BaseGameAction
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode

@Keep
sealed class CoachPlusActions : BaseGameAction {
    data class SomethingWrong(val error: Throwable) : CoachPlusActions()
    object Cancel : CoachPlusActions()
    data class OpenSettings(val toothbrushMac: String?) : CoachPlusActions()
    object DataSaved : CoachPlusActions()
    object Restarted : CoachPlusActions()
    data class ShowBrushingModeDialog(
        val brushingModes: List<BrushingMode>,
        val currentMode: BrushingMode
    ) : CoachPlusActions()
}

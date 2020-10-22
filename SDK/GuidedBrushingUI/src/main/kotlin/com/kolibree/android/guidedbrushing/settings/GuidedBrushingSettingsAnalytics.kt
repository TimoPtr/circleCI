/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.settings

import com.kolibree.android.tracker.Analytics.send
import com.kolibree.android.tracker.AnalyticsEvent

internal object GuidedBrushingSettingsAnalytics {
    fun main() = AnalyticsEvent(name = "GBSetting")
    private val brushingMovementsEvent = main() + "BrushingMoveDisplay"
    private val helpTextsEvent = main() + "LiveFeedbackDisplay"
    private val musicEvent = main() + "Music"
    private val transitionSoundsEvent = main() + "SoundTransition"

    fun brushingMovements(isOn: Boolean) = send(brushingMovementsEvent + state(isOn))
    fun helpTexts(isOn: Boolean) = send(helpTextsEvent + state(isOn))
    fun music(isOn: Boolean) = send(musicEvent + state(isOn))
    fun transitionSounds(isOn: Boolean) = send(transitionSoundsEvent + state(isOn))
    fun goBack() = send(main() + "GoBack")
    fun chooseMusic() = send(main() + "ChooseMusic")

    private fun state(isOn: Boolean): String = if (isOn) "On" else "Off"
}

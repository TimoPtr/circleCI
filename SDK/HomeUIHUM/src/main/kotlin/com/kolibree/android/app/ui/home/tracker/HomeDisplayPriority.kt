/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tracker

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.celebration.EarnPointsCelebrationActivity
import com.kolibree.android.offlinebrushings.ExtractionProgress
import com.kolibree.android.utils.KLItem
import com.kolibree.android.utils.Priority

@VisibleForApp
sealed class HomeDisplayPriority(override val priority: Priority) : KLItem {

    /**
     * Displays [HumTestBrushingStartScreenActivity]
     */
    internal object TestBrushing : HomeDisplayPriority(Priority.URGENT)

    /**
     * Displays the Toolbox Explanation overlay
     */
    internal object ToolboxItem : HomeDisplayPriority(Priority.HIGH)

    /**
     * Displays [EarnPointsCelebrationActivity]
     */
    internal object Celebration : HomeDisplayPriority(Priority.MEDIUM)

    /**
     * Displays [CheckupActivity] for retrieved offline brushing
     */
    data class OfflineBrushing(val extractionProgress: ExtractionProgress) :
        HomeDisplayPriority(Priority.LOW)

    /**
     * Displays the Low Battery Dialog
     */
    internal object LowBatteryItem : HomeDisplayPriority(Priority.LOW)

    /**
     * Displays the Replace Toothbrush Head Dialog
     */
    internal object ReplaceHeadItem : HomeDisplayPriority(Priority.LOW)
}

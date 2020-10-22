/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.lastbrushing

import com.kolibree.android.app.ui.card.DynamicCardInteraction

/** Last Brushing Card [DynamicCardInteraction] implementation */
internal interface LastBrushingCardInteraction : DynamicCardInteraction, BrushingTopSectionInteraction

internal interface BrushingTopSectionInteraction {
    fun onTopBrushingItemClick(position: Int, item: BrushingCardData)

    fun onDeleteBrushingSessionClick()

    fun onPulsingDotClick()
}

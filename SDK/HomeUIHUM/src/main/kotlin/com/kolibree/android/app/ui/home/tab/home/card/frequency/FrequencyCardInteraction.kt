/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.frequency

import com.kolibree.android.app.ui.card.DynamicCardInteraction
import com.kolibree.android.app.ui.home.tab.view.PageableHeaderInteraction

internal interface FrequencyCardInteraction : DynamicCardInteraction,
    PageableHeaderInteraction,
    FrequencyChartInteraction {

    fun onPulsingDotClick()
}

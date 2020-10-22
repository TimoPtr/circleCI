/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.earningpoints

import android.view.View
import com.kolibree.android.app.ui.card.DynamicCardInteraction

internal interface EarningPointsCardInteraction :
    DynamicCardInteraction {

    fun toggleExpanded(view: View)

    fun onTermsAndConditionsClick()
}

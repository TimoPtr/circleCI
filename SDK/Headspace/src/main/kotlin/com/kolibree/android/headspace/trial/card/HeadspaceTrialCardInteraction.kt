/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.trial.card

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.card.DynamicCardInteraction

@VisibleForApp
interface HeadspaceTrialCardInteraction : DynamicCardInteraction {

    fun onToggleDescriptionClick()
    fun onCallToActionClicked()
    fun onTapToCopyClicked()
    fun onCloseClicked()
}

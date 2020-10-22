/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.brushingstreak

import android.view.View
import com.kolibree.android.app.ui.card.DynamicCardInteraction
import com.kolibree.android.rewards.personalchallenge.presentation.CompletedChallenge
import com.kolibree.android.rewards.personalchallenge.presentation.NotAcceptedChallenge
import com.kolibree.android.rewards.personalchallenge.presentation.OnGoingChallenge

internal interface BrushingStreakCardInteraction : DynamicCardInteraction, ChallengeInteraction {
    fun toggleExpanded(view: View)
}

internal interface ChallengeInteraction {
    fun onAcceptChallengeClick(challenge: NotAcceptedChallenge)
    fun onActionClick(challenge: OnGoingChallenge)
    fun onCompleteChallengeClick(challenge: CompletedChallenge)
}

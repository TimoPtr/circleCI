/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.profile.card

import androidx.lifecycle.ViewModel
import com.kolibree.android.app.ui.DynamicCardListConfiguration
import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.app.ui.home.tab.home.card.frequency.FrequencyCardViewModel
import com.kolibree.android.app.ui.home.tab.home.card.lastbrushing.LastBrushingCardViewModel
import com.kolibree.android.app.ui.home.tab.profile.card.lifetimestats.LifetimeStatsCardViewModel

internal object ProfileDynamicCardListConfiguration :
    DynamicCardListConfiguration {

    override fun <T : ViewModel> getInitialCardPosition(cardViewModelClass: Class<T>): DynamicCardPosition =
        when (cardViewModelClass) {
            LifetimeStatsCardViewModel::class.java -> DynamicCardPosition.ZERO
            FrequencyCardViewModel::class.java -> DynamicCardPosition.ONE
            LastBrushingCardViewModel::class.java -> DynamicCardPosition.SIX
            else -> throw IllegalStateException(
                "Card represented by ${cardViewModelClass.simpleName} " +
                    "doesn't have display position assigned on profile tab. " +
                    "Please add it to the ProfileTabConfiguration."
            )
        }
}

/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying thfile via any medium without the prior written consent of Kolibree strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card

import androidx.lifecycle.ViewModel
import com.kolibree.android.app.ui.DynamicCardListConfiguration
import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.app.ui.home.tab.home.card.brushbetter.BrushBetterCardViewModel
import com.kolibree.android.app.ui.home.tab.home.card.brushingstreak.BrushingStreakCardViewModel
import com.kolibree.android.app.ui.home.tab.home.card.earningpoints.EarningPointsCardViewModel
import com.kolibree.android.app.ui.home.tab.home.card.frequency.FrequencyCardViewModel
import com.kolibree.android.app.ui.home.tab.home.card.lastbrushing.LastBrushingCardViewModel
import com.kolibree.android.app.ui.home.tab.home.card.morewaystoearnpoints.MoreWaysToEarnPointsCardViewModel
import com.kolibree.android.app.ui.home.tab.home.card.question.QuestionCardViewModel
import com.kolibree.android.app.ui.home.tab.home.card.rewardyourself.RewardYourselfCardViewModel
import com.kolibree.android.app.ui.home.tab.home.card.support.oralcare.OralCareSupportCardViewModel
import com.kolibree.android.app.ui.home.tab.home.card.support.product.ProductSupportCardViewModel
import com.kolibree.android.headspace.mindful.ui.card.HeadspaceMindfulMomentCardViewModel
import com.kolibree.android.headspace.trial.card.HeadspaceTrialCardViewModel

internal object HomeDynamicCardListConfiguration :
    DynamicCardListConfiguration {

    @Suppress("ComplexMethod", "LongMethod")
    override fun <T : ViewModel> getInitialCardPosition(cardViewModelClass: Class<T>): DynamicCardPosition =
        when (cardViewModelClass) {
            EarningPointsCardViewModel::class.java -> DynamicCardPosition.ZERO
            BrushingStreakCardViewModel::class.java -> DynamicCardPosition.ONE
            LastBrushingCardViewModel::class.java -> DynamicCardPosition.TWO
            HeadspaceMindfulMomentCardViewModel::class.java -> DynamicCardPosition.THREE
            HeadspaceTrialCardViewModel::class.java -> DynamicCardPosition.FOUR
            BrushBetterCardViewModel::class.java -> DynamicCardPosition.FIVE
            FrequencyCardViewModel::class.java -> DynamicCardPosition.SIX
            MoreWaysToEarnPointsCardViewModel::class.java -> DynamicCardPosition.SEVEN
            QuestionCardViewModel::class.java -> DynamicCardPosition.EIGHT
            RewardYourselfCardViewModel::class.java -> DynamicCardPosition.NINE
            ProductSupportCardViewModel::class.java -> DynamicCardPosition.TEN
            OralCareSupportCardViewModel::class.java -> DynamicCardPosition.ELEVEN
            else -> throw IllegalStateException(
                "Card represented by ${cardViewModelClass.simpleName} " +
                    "doesn't have display position assigned on home tab. " +
                    "Please add it to the HomeTabConfiguration."
            )
        }
}

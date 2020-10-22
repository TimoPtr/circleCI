/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.morewaystoearnpoints

import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.AMAZON_DASH
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.COMPLETE_YOUR_PROFILE
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.RATE_THE_APP
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.REFER_A_FRIEND
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.SUBSCRIBE_FOR_WEEKLY_REVIEW
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.TURN_ON_BRUSHING_REMINDERS
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.TURN_ON_BRUSH_SYNC_REMINDERS
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.TURN_ON_EMAIL_NOTIFICATIONS
import com.kolibree.android.tracker.AnalyticsEvent

internal object MoreWaysToEarnPointsCardAnalytics {

    private fun main() = AnalyticsEvent("WaysToEarnPoints")

    fun challengeCardClick(id: EarnPointsChallenge.Id): AnalyticsEvent = main() + when (id) {
        COMPLETE_YOUR_PROFILE -> "CompleteProfile"
        TURN_ON_EMAIL_NOTIFICATIONS -> "EmailNotification"
        TURN_ON_BRUSH_SYNC_REMINDERS -> "BrushSync"
        TURN_ON_BRUSHING_REMINDERS -> "BrushReminders"
        RATE_THE_APP -> "RateApp"
        SUBSCRIBE_FOR_WEEKLY_REVIEW -> "WeeklyReview"
        REFER_A_FRIEND -> "ReferFriend"
        AMAZON_DASH -> "AmazonDash"
    }
}

/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.morewaystoearnpoints

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.AMAZON_DASH
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.COMPLETE_YOUR_PROFILE
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.RATE_THE_APP
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.REFER_A_FRIEND
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.SUBSCRIBE_FOR_WEEKLY_REVIEW
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.TURN_ON_BRUSHING_REMINDERS
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.TURN_ON_BRUSH_SYNC_REMINDERS
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.TURN_ON_EMAIL_NOTIFICATIONS
import javax.inject.Inject

@VisibleForApp
interface MoreWaysToEarnPointsCardItemResourceProvider {

    @DrawableRes
    fun getIcon(cardId: EarnPointsChallenge.Id): Int

    @StringRes
    fun getHeader(cardId: EarnPointsChallenge.Id): Int

    @StringRes
    fun getBody(cardId: EarnPointsChallenge.Id): Int
}

@VisibleForApp
class MoreWaysToEarnPointsCardItemResourceProviderImpl @Inject constructor() :
    MoreWaysToEarnPointsCardItemResourceProvider {

    override fun getIcon(cardId: EarnPointsChallenge.Id): Int = when (cardId) {
        TURN_ON_BRUSHING_REMINDERS -> R.drawable.ic_more_ways_brushing_reminders
        TURN_ON_EMAIL_NOTIFICATIONS -> R.drawable.ic_more_ways_email_reminders
        TURN_ON_BRUSH_SYNC_REMINDERS -> R.drawable.ic_more_ways_sync_reminders
        REFER_A_FRIEND -> R.drawable.ic_more_ways_refer_friend
        RATE_THE_APP -> R.drawable.ic_more_ways_rate_app
        SUBSCRIBE_FOR_WEEKLY_REVIEW -> R.drawable.ic_more_ways_weekly_review
        COMPLETE_YOUR_PROFILE -> R.drawable.ic_more_ways_complete_profile
        AMAZON_DASH -> R.drawable.ic_more_ways_amazon_dash
    }

    override fun getHeader(cardId: EarnPointsChallenge.Id): Int = when (cardId) {
        TURN_ON_BRUSHING_REMINDERS -> R.string.more_ways_to_earn_points_brushing_reminders_header
        TURN_ON_EMAIL_NOTIFICATIONS -> R.string.more_ways_to_earn_points_email_notifications_header
        TURN_ON_BRUSH_SYNC_REMINDERS -> R.string.more_ways_to_earn_points_brush_sync_reminders_header
        REFER_A_FRIEND -> R.string.more_ways_to_earn_points_refer_a_friend_header
        RATE_THE_APP -> R.string.more_ways_to_earn_points_rate_the_app_header
        SUBSCRIBE_FOR_WEEKLY_REVIEW -> R.string.more_ways_to_earn_points_weekly_review_header
        COMPLETE_YOUR_PROFILE -> R.string.more_ways_to_earn_points_complete_profile_header
        AMAZON_DASH -> R.string.more_ways_to_earn_points_amazon_dash_header
    }

    override fun getBody(cardId: EarnPointsChallenge.Id): Int = when (cardId) {
        TURN_ON_BRUSHING_REMINDERS -> R.string.more_ways_to_earn_points_brushing_reminders_body
        TURN_ON_EMAIL_NOTIFICATIONS -> R.string.more_ways_to_earn_points_email_notifications_body
        TURN_ON_BRUSH_SYNC_REMINDERS -> R.string.more_ways_to_earn_points_brush_sync_reminders_body
        REFER_A_FRIEND -> R.string.more_ways_to_earn_points_refer_a_friend_body
        RATE_THE_APP -> R.string.more_ways_to_earn_points_rate_the_app_body
        SUBSCRIBE_FOR_WEEKLY_REVIEW -> R.string.more_ways_to_earn_points_weekly_review_body
        COMPLETE_YOUR_PROFILE -> R.string.more_ways_to_earn_points_complete_profile_body
        AMAZON_DASH -> R.string.more_ways_to_earn_points_amazon_dash_body
    }
}

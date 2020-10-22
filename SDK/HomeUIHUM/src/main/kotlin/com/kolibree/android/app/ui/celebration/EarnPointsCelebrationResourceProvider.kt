/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.celebration

import androidx.annotation.StringRes
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

internal interface EarnPointsCelebrationResourceProvider {

    @StringRes
    fun getName(challenge: EarnPointsChallenge): Int
}

internal class EarnPointsCelebrationResourceProviderImpl @Inject constructor() :
    EarnPointsCelebrationResourceProvider {

    override fun getName(challenge: EarnPointsChallenge): Int {
        return when (challenge.id) {
            COMPLETE_YOUR_PROFILE -> R.string.earn_points_celebration_complete_profile_name
            TURN_ON_EMAIL_NOTIFICATIONS -> R.string.earn_points_celebration_email_notifications_name
            TURN_ON_BRUSH_SYNC_REMINDERS -> R.string.earn_points_celebration_brush_sync_reminders_name
            TURN_ON_BRUSHING_REMINDERS -> R.string.earn_points_celebration_brushing_reminders_name
            RATE_THE_APP -> R.string.earn_points_celebration_rate_the_app_name
            SUBSCRIBE_FOR_WEEKLY_REVIEW -> R.string.earn_points_celebration_weekly_review_name
            REFER_A_FRIEND -> R.string.earn_points_celebration_refer_a_friend_name
            AMAZON_DASH -> R.string.earn_points_celebration_amazon_dash
        }
    }
}

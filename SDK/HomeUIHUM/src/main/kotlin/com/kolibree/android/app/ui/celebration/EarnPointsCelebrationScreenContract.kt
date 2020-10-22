/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.celebration

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.rewards.morewaystoearnpoints.model.CompleteEarnPointsChallenge

@VisibleForApp
class EarnPointsCelebrationScreenContract :
    ActivityResultContract<List<CompleteEarnPointsChallenge>, Unit>() {

    override fun createIntent(
        context: Context,
        challenges: List<CompleteEarnPointsChallenge>
    ): Intent {
        return Intent(context, EarnPointsCelebrationActivity::class.java).apply {
            putParcelableArrayListExtra(EXTRA_CHALLENGES, ArrayList(challenges.toMutableList()))
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?) = Unit
}

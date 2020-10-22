/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.questionoftheday.ui

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDay
import com.kolibree.android.questionoftheday.ui.QuestionOfTheDayScreenResult.Closed
import com.kolibree.android.questionoftheday.ui.QuestionOfTheDayScreenResult.Collected
import org.threeten.bp.OffsetDateTime

@VisibleForApp
class QuestionOfTheDayScreenContract : ActivityResultContract<
    QuestionOfTheDay,
    QuestionOfTheDayScreenResult>() {

    override fun createIntent(context: Context, input: QuestionOfTheDay): Intent {
        return questionOfTheDayScreenIntent(context, input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): QuestionOfTheDayScreenResult {
        return when (resultCode) {
            RESULT_OK -> Collected(intent?.getSerializableExtra(EXTRA_ANSWER_TIME) as OffsetDateTime)
            else -> Closed
        }
    }
}

@VisibleForApp
sealed class QuestionOfTheDayScreenResult {
    @VisibleForApp
    class Collected(val answerTime: OffsetDateTime) : QuestionOfTheDayScreenResult()

    @VisibleForApp
    object Closed : QuestionOfTheDayScreenResult()
}

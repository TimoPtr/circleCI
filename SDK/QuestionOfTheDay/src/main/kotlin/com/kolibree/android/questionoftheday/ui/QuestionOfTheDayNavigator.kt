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
import android.content.Intent
import com.kolibree.android.app.base.BaseNavigator
import org.threeten.bp.OffsetDateTime

internal class QuestionOfTheDayNavigator : BaseNavigator<QuestionOfTheDayActivity>() {

    fun finish() = withOwner {
        finish()
    }

    fun finishWithSuccess(answerTime: OffsetDateTime) = withOwner {
        val data = Intent().apply {
            putExtra(EXTRA_ANSWER_TIME, answerTime)
        }
        setResult(RESULT_OK, data)
        finish()
    }
}

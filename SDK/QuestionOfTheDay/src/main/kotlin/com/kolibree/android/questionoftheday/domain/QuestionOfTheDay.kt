/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.questionoftheday.domain

import android.os.Parcelable
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.failearly.FailEarly
import kotlinx.android.parcel.Parcelize

@VisibleForApp
@Parcelize
data class QuestionOfTheDay(
    val id: Long,
    val question: String,
    val points: Int,
    val answers: List<Answer>
) : Parcelable {

    init {
        val correctCount = answers.count { it.correct }
        check(correctCount > 0) { "Required answer is missing!" }
        FailEarly.failInConditionMet(
            condition = correctCount > 1,
            message = "Only one answer can be correct! Correct answers found: $correctCount"
        )
    }

    @VisibleForApp
    @Parcelize
    data class Answer(
        val id: Long,
        val text: String,
        val correct: Boolean = false
    ) : Parcelable
}

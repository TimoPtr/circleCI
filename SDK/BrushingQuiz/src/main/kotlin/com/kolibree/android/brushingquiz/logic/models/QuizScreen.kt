/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.logic.models

import android.os.Parcelable
import androidx.annotation.StringRes
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class QuizScreen(
    @StringRes val title: Int,
    private val questionsMap: Map<Int, QuizAnswer>,
    val answer: QuizAnswer? = null
) : Parcelable {
    @IgnoredOnParcel
    val sortedQuestions: List<QuizAnswer> = questionsMap.toSortedMap().values.toList()

    fun getAnswerIndex(answer: QuizAnswer) =
        questionsMap
            .entries
            .first { it.value.same(answer) }
            .key

    fun withAnswer(answer: QuizAnswer): QuizScreen =
        copy(answer = answer,
            questionsMap = questionsMap
                .mapValues { entry ->
                    val selected = entry.value.same(answer)

                    entry.value.copy(selected = entry.value.same(answer))
                }
        )
}

private fun QuizAnswer.same(other: QuizAnswer) = other.message == message

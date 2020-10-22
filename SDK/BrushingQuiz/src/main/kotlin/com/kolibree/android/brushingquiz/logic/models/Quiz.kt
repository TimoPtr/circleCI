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
import com.kolibree.android.brushingquiz.logic.QuizScreenProvider
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class Quiz(
    private val screensMap: Map<Int, QuizScreen> = QuizScreenProvider.quizScreenMap
) : Parcelable {

    /**
     * Returns the index of the last screen without [QuizAnswer]. If all screens have answers,
     * it'll return the index of the last [QuizScreen]
     *
     * If no screen has answer, it'll return 0
     */
    @IgnoredOnParcel
    val nextScreenIndex: Int = firstEntryWithoutAnswer()?.key ?: screensMap.size - 1

    @IgnoredOnParcel
    val sortedScreens: List<QuizScreen> = screensMap.toSortedMap().values.toList()

    /**
     * Returns a [Quiz] with the answer to the current screen and the next
     * [QuizScreen] to display
     *
     * If the ViewState is already at the last [QuizScreen], it'll return a [Quiz]
     * with the same [QuizScreen] and the [QuizAnswer] updated
     */
    fun withAnswer(screenIndex: Int, answer: QuizAnswer): Quiz {
        return copy(screensMap = screensMap
            .mapValues {
                return@mapValues if (it.key == screenIndex)
                    it.value.withAnswer(answer = answer)
                else
                    it.value
            })
    }

    /**
     * Get a [QuizAnswer] index in the screen
     *
     * @param screenIndex [Int] question index
     * @param answer [QuizAnswer]
     * @return [Int] answer index
     */
    fun getAnswerIndex(screenIndex: Int, answer: QuizAnswer) =
        screensMap[screenIndex]
            ?.getAnswerIndex(answer)
            ?: 0

    /**
     * Returns a [Quiz] with [QuizAnswer] cleared from the latest [QuizScreen] with answer
     *
     * If there's no [QuizScreen] with [QuizAnswer] different than null, it returns the same
     * instance
     */
    fun withLastAnswerCleared(): Quiz {
        val lastScreenWithAnswer = lastEntryWithAnswer() ?: return this

        val newScreens = screensMap.toMutableMap().apply {
            put(lastScreenWithAnswer.key, lastScreenWithAnswer.value.copy(answer = null))
        }
            .toMap()

        return copy(screensMap = newScreens)
    }

    private fun lastEntryWithAnswer(): Map.Entry<Int, QuizScreen>? {
        return screensMap
            .filterValues { it.answer != null }
            .maxBy { it.key }
    }

    private fun firstEntryWithoutAnswer(): Map.Entry<Int, QuizScreen>? {
        return screensMap
            .filterValues { it.answer == null }
            .minBy { it.key }
    }

    /**
     * @return recommended [BrushingMode] after completing the [Quiz], or null if [Quiz] isn't
     * completed yet
     */
    fun selectedBrushingMode(): BrushingMode? {
        val lastQuizScreen = screensMap[LAST_QUESTION_INDEX]

        return lastQuizScreen?.answer?.run {
            @Suppress("MoveVariableDeclarationIntoWhen")
            val answerIndex = lastQuizScreen.sortedQuestions.indexOfFirst { answer ->
                this.message == answer.message
            }

            when (answerIndex) {
                0 -> BrushingMode.Strong
                1 -> BrushingMode.Regular
                else -> BrushingMode.Slow
            }
        }
    }
}

private const val LAST_QUESTION_INDEX = 2

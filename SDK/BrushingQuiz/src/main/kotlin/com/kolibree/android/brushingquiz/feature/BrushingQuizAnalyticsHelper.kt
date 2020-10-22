/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.feature

import androidx.annotation.VisibleForTesting
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.EventTracker
import javax.inject.Inject

/** Wrapper class for Google Analytics event and screen names */
internal interface BrushingQuizAnalyticsHelper {

    /**
     * Get the Analytics screen name for a given brushing quiz page index
     *
     * @param quizPageIndex [Int]
     * @return screen name [String]
     */
    fun getScreenNameForQuizQuestionIndex(quizPageIndex: Int): AnalyticsEvent

    /**
     * Get the Analytics screen name for the quiz result page
     *
     * @return screen name [String]
     */
    fun getScreenNameForQuizResult(): AnalyticsEvent

    /**
     * To be called when the user clicks on a quiz answer
     *
     * @param questionIndex [Int] question index
     * @param answerIndex [Int] answer index
     */
    fun onQuestionAnswered(questionIndex: Int, answerIndex: Int)

    /**
     * To be called when the user clicks on the confirm brushing mode button
     */
    fun onConfirmButtonClick()

    /**
     * To be called when the user clicks on the revert brushing mode button
     */
    fun onRevertButtonClick()

    /**
     * To be called when the user clicks on the try brushing mode button
     */
    fun onTryButtonClick()

    /**
     * To be called when the user clicks back on specific program
     *
     * @param programIndex [Int] program index
     */
    fun onGoBackFromProgram(programIndex: Int)
}

/** [BrushingQuizAnalyticsHelper] implementation */
internal class BrushingQuizAnalyticsHelperImpl @Inject constructor(
    private val tracker: EventTracker
) : BrushingQuizAnalyticsHelper {

    override fun getScreenNameForQuizQuestionIndex(quizPageIndex: Int) =
        when (quizPageIndex) {
            0 -> SCREEN_NAME_QUESTION_1
            1 -> SCREEN_NAME_QUESTION_2
            2 -> SCREEN_NAME_QUESTION_3
            else -> throw IllegalArgumentException()
        }

    override fun getScreenNameForQuizResult() = SCREEN_NAME_QUIZ_RESULT

    override fun onQuestionAnswered(questionIndex: Int, answerIndex: Int) =
        if (questionIndex in 0..MAX_QUESTION_INDEX && answerIndex in 0..MAX_ANSWER_INDEX) {
            tracker.sendEvent(answerEventsIndexMap[questionIndex][answerIndex])
        } else {
            FailEarly.fail(exception = IndexOutOfBoundsException())
        }

    override fun onConfirmButtonClick() = tracker.sendEvent(EVENT_RESULT_CONFIRM)

    override fun onRevertButtonClick() = tracker.sendEvent(EVENT_RESULT_REVERT)

    override fun onTryButtonClick() = tracker.sendEvent(EVENT_RESULT_TRY)

    override fun onGoBackFromProgram(programIndex: Int) {
        val programNumber = programIndex + 1
        tracker.sendEvent(goBackFromProgramEvent(programNumber))
    }

    companion object {

        @VisibleForTesting
        val FEATURE = AnalyticsEvent("BrushingProgram")

        /*
        Screen names
         */

        @VisibleForTesting
        val SCREEN_NAME_QUESTION_1 = FEATURE + "Quiz1"

        @VisibleForTesting
        val SCREEN_NAME_QUESTION_2 = FEATURE + "Quiz2"

        @VisibleForTesting
        val SCREEN_NAME_QUESTION_3 = FEATURE + "Quiz3"

        @VisibleForTesting
        val SCREEN_NAME_QUIZ_RESULT = FEATURE + "QuizResult"

        /*
        Events
        */

        @VisibleForTesting
        val EVENT_QUESTION_1_ANSWER_1 = SCREEN_NAME_QUESTION_1 + "Option1"

        @VisibleForTesting
        val EVENT_QUESTION_1_ANSWER_2 = SCREEN_NAME_QUESTION_1 + "Option2"

        @VisibleForTesting
        val EVENT_QUESTION_1_ANSWER_3 = SCREEN_NAME_QUESTION_1 + "Option3"

        @VisibleForTesting
        val EVENT_QUESTION_2_ANSWER_1 = SCREEN_NAME_QUESTION_2 + "Option1"

        @VisibleForTesting
        val EVENT_QUESTION_2_ANSWER_2 = SCREEN_NAME_QUESTION_2 + "Option2"

        @VisibleForTesting
        val EVENT_QUESTION_2_ANSWER_3 = SCREEN_NAME_QUESTION_2 + "Option3"

        @VisibleForTesting
        val EVENT_QUESTION_3_ANSWER_1 = SCREEN_NAME_QUESTION_3 + "Option1"

        @VisibleForTesting
        val EVENT_QUESTION_3_ANSWER_2 = SCREEN_NAME_QUESTION_3 + "Option2"

        @VisibleForTesting
        val EVENT_QUESTION_3_ANSWER_3 = SCREEN_NAME_QUESTION_3 + "Option3"

        @VisibleForTesting
        val EVENT_RESULT_CONFIRM = SCREEN_NAME_QUIZ_RESULT + "Confirm"

        @VisibleForTesting
        val EVENT_RESULT_REVERT = SCREEN_NAME_QUIZ_RESULT + "Revert"

        @VisibleForTesting
        val EVENT_RESULT_TRY = SCREEN_NAME_QUIZ_RESULT + "Try"

        @VisibleForTesting
        fun goBackFromProgramEvent(programIndex: Int) =
            AnalyticsEvent(name = "BrushProg$programIndex") + "GoBack"

        private val answerEventsIndexMap =
            listOf(
                listOf(
                    EVENT_QUESTION_1_ANSWER_1,
                    EVENT_QUESTION_1_ANSWER_2,
                    EVENT_QUESTION_1_ANSWER_3
                ),
                listOf(
                    EVENT_QUESTION_2_ANSWER_1,
                    EVENT_QUESTION_2_ANSWER_2,
                    EVENT_QUESTION_2_ANSWER_3
                ),
                listOf(
                    EVENT_QUESTION_3_ANSWER_1,
                    EVENT_QUESTION_3_ANSWER_2,
                    EVENT_QUESTION_3_ANSWER_3
                )
            )

        private const val MAX_QUESTION_INDEX = 2

        private const val MAX_ANSWER_INDEX = 2
    }
}

/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.logic

import com.kolibree.android.brushingquiz.R
import com.kolibree.android.brushingquiz.logic.models.QuizAnswer
import com.kolibree.android.brushingquiz.logic.models.QuizScreen

internal object QuizScreenProvider {
    val quizScreenMap = mutableMapOf<Int, QuizScreen>().apply {
        put(0, QuizScreen(
            title = R.string.brushing_quiz_screen_1_title,
            questionsMap = mutableMapOf<Int, QuizAnswer>().apply {
                put(
                    0,
                    QuizAnswer(
                        message = R.string.brushing_quiz_screen_1_answer_1,
                        hint = R.string.brushing_quiz_screen_1_answer_1_hint,
                        isFirst = true
                    )
                )
                put(
                    1,
                    QuizAnswer(
                        message = R.string.brushing_quiz_screen_1_answer_2,
                        hint = R.string.brushing_quiz_screen_1_answer_2_hint
                    )
                )
                put(
                    2,
                    QuizAnswer(
                        message = R.string.brushing_quiz_screen_1_answer_3,
                        hint = R.string.brushing_quiz_screen_1_answer_3_hint,
                        isLast = true
                    )
                )
            }
        ))

        put(1, QuizScreen(
            title = R.string.brushing_quiz_screen_2_title,
            questionsMap = mutableMapOf<Int, QuizAnswer>().apply {
                put(
                    0,
                    QuizAnswer(
                        message = R.string.brushing_quiz_screen_2_answer_1,
                        hint = R.string.brushing_quiz_screen_2_answer_1_hint,
                        isFirst = true
                    )
                )
                put(
                    1,
                    QuizAnswer(
                        message = R.string.brushing_quiz_screen_2_answer_2,
                        hint = R.string.brushing_quiz_screen_2_answer_2_hint
                    )
                )
                put(
                    2,
                    QuizAnswer(
                        message = R.string.brushing_quiz_screen_2_answer_3,
                        hint = R.string.brushing_quiz_screen_2_answer_3_hint,
                        isLast = true
                    )
                )
            }
        ))

        put(2, QuizScreen(
            title = R.string.brushing_quiz_screen_3_title,
            questionsMap = mutableMapOf<Int, QuizAnswer>().apply {
                put(
                    0,
                    QuizAnswer(
                        message = R.string.brushing_quiz_screen_3_answer_1,
                        hint = R.string.brushing_quiz_screen_3_answer_1_hint,
                        isFirst = true
                    )
                )
                put(
                    1,
                    QuizAnswer(
                        message = R.string.brushing_quiz_screen_3_answer_2,
                        hint = R.string.brushing_quiz_screen_3_answer_2_hint
                    )
                )
                put(
                    2,
                    QuizAnswer(
                        message = R.string.brushing_quiz_screen_3_answer_3,
                        hint = R.string.brushing_quiz_screen_3_answer_3_hint,
                        isLast = true
                    )
                )
            }
        ))
    }.toMap()

    fun provideScreens(): Map<Int, QuizScreen> = quizScreenMap
}

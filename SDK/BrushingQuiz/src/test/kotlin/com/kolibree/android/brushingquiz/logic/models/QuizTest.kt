/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.logic.models

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.brushingquiz.R
import com.kolibree.android.brushingquiz.logic.QuizScreenProvider
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import junit.framework.TestCase.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizTest : BaseUnitTest() {
    private lateinit var quiz: Quiz

    /*
    withAnswer
     */
    @Test
    fun `withAnswer stores answer to screen`() {
        quiz = defaultQuiz()

        val screenIndex = 0
        val firstScreen = quiz.sortedScreens[screenIndex]

        assertNull(firstScreen.answer)

        val expectedAnswer = firstScreen.sortedQuestions.random()
        val newQuiz = quiz.withAnswer(screenIndex, expectedAnswer)

        assertQuizScreens(
            firstScreen.copy(answer = expectedAnswer),
            newQuiz.sortedScreens[screenIndex]
        )
    }

    @Test
    fun `withAnswer replaces previous answer of screen`() {
        val index = 1
        quiz = Quiz(QuizScreenProvider.quizScreenMap
            .toMutableMap()
            .apply {
                val secondScreen = QuizScreenProvider.quizScreenMap.getValue(index)

                replace(index, secondScreen.copy(answer = secondScreen.sortedQuestions[0]))
            }
            .toMap()
        )

        val secondScreen = quiz.sortedScreens[index]
        val expectedAnswer = secondScreen.sortedQuestions[1]
        val newQuiz = quiz.withAnswer(index, expectedAnswer)

        val expectedScreen = secondScreen.copy(answer = expectedAnswer)

        assertQuizScreens(
            expectedScreen,
            newQuiz.sortedScreens[index]
        )
    }

    /*
    withLastAnswerCleared
     */
    @Test
    fun `withLastAnswerCleared returns same instance if no screen has answer`() {
        quiz = defaultQuiz()

        assertEquals(quiz, quiz.withLastAnswerCleared())
    }

    @Test
    fun `withLastAnswerCleared removes answer from the entry with highest index`() {
        quiz = defaultQuiz()

        val firstScreen = quiz.sortedScreens[0]
        val secondScreen = quiz.sortedScreens[1]

        val answerToFirstScreen = firstScreen.sortedQuestions[0]
        val answerToSecondScreen = secondScreen.sortedQuestions[0]
        quiz = quiz
            .withAnswer(0, answerToFirstScreen)
            .withAnswer(1, answerToSecondScreen)

        assertQuizScreens(firstScreen.copy(answer = answerToFirstScreen), quiz.sortedScreens[0])
        assertQuizScreens(secondScreen.copy(answer = answerToSecondScreen), quiz.sortedScreens[1])

        var newQuiz = quiz.withLastAnswerCleared()

        assertQuizScreens(firstScreen.copy(answer = answerToFirstScreen), newQuiz.sortedScreens[0])
        assertQuizScreens(secondScreen.copy(answer = null), newQuiz.sortedScreens[1])

        newQuiz = newQuiz.withLastAnswerCleared()

        assertQuizScreens(firstScreen.copy(answer = null), newQuiz.sortedScreens[0])
        assertQuizScreens(secondScreen.copy(answer = null), newQuiz.sortedScreens[1])
    }

    private fun assertQuizScreens(expected: QuizScreen, current: QuizScreen) {
        val areTheSame = true
        assertEquals(expected.answer, current.answer)
        assertEquals(expected.title, current.title)
        assertEquals(expected.sortedQuestions.size, current.sortedQuestions.size)
        for (answerIndex in expected.sortedQuestions.indices) {
            val expectedAnswer = expected.sortedQuestions[answerIndex]
            val currentAnswer = expected.sortedQuestions[answerIndex]
            assertEquals(expectedAnswer.message, currentAnswer.message)
        }

        assertTrue(areTheSame)
    }

    /*
    withAnswer
     */
    @Test
    fun `nextScreenIndex returns 0 if no screen has answer`() {
        quiz = defaultQuiz()

        assertEquals(0, quiz.nextScreenIndex)
    }

    @Test
    fun `nextScreenIndex returns first screen without answer`() {
        quiz = Quiz(mutableMapOf<Int, QuizScreen>().apply {
            put(
                0, QuizScreen(
                    title = R.string.brushing_quiz_screen_1_title,
                    questionsMap = mutableMapOf<Int, QuizAnswer>().apply {
                        put(
                            0,
                            QuizAnswer(
                                R.string.brushing_quiz_screen_1_answer_1,
                                R.string.brushing_quiz_screen_1_answer_1_hint
                            )
                        )
                        put(
                            1,
                            QuizAnswer(
                                R.string.brushing_quiz_screen_1_answer_2,
                                R.string.brushing_quiz_screen_1_answer_2_hint
                            )
                        )
                        put(
                            2,
                            QuizAnswer(
                                R.string.brushing_quiz_screen_1_answer_3,
                                R.string.brushing_quiz_screen_1_answer_3_hint
                            )
                        )
                    },
                    answer = QuizAnswer(
                        R.string.brushing_quiz_screen_1_answer_1,
                        R.string.brushing_quiz_screen_1_answer_1_hint
                    )
                )
            )

            put(1, QuizScreen(
                title = R.string.brushing_quiz_screen_2_title,
                questionsMap = mutableMapOf<Int, QuizAnswer>().apply {
                    put(
                        0,
                        QuizAnswer(
                            R.string.brushing_quiz_screen_2_answer_1,
                            R.string.brushing_quiz_screen_2_answer_1_hint
                        )
                    )
                    put(
                        1,
                        QuizAnswer(
                            R.string.brushing_quiz_screen_2_answer_2,
                            R.string.brushing_quiz_screen_2_answer_2_hint
                        )
                    )
                    put(
                        2,
                        QuizAnswer(
                            R.string.brushing_quiz_screen_2_answer_3,
                            R.string.brushing_quiz_screen_2_answer_3_hint
                        )
                    )
                }
            ))

            put(2, QuizScreen(
                title = R.string.brushing_quiz_screen_2_title,
                questionsMap = mutableMapOf<Int, QuizAnswer>().apply {
                    put(
                        0,
                        QuizAnswer(
                            R.string.brushing_quiz_screen_2_answer_1,
                            R.string.brushing_quiz_screen_2_answer_1_hint
                        )
                    )
                    put(
                        1,
                        QuizAnswer(
                            R.string.brushing_quiz_screen_2_answer_2,
                            R.string.brushing_quiz_screen_2_answer_2_hint
                        )
                    )
                    put(
                        2,
                        QuizAnswer(
                            R.string.brushing_quiz_screen_2_answer_3,
                            R.string.brushing_quiz_screen_2_answer_3_hint
                        )
                    )
                }
            ))
        }
        )

        assertEquals(1, quiz.nextScreenIndex)
    }

    @Test
    fun `nextScreenIndex returns last index if all screens have answer`() {
        quiz = Quiz(mutableMapOf<Int, QuizScreen>().apply {
            put(
                0, QuizScreen(
                    title = R.string.brushing_quiz_screen_1_title,
                    questionsMap = mutableMapOf<Int, QuizAnswer>().apply {
                        put(
                            0,
                            QuizAnswer(
                                R.string.brushing_quiz_screen_1_answer_1,
                                R.string.brushing_quiz_screen_1_answer_1_hint
                            )
                        )
                        put(
                            1,
                            QuizAnswer(
                                R.string.brushing_quiz_screen_1_answer_2,
                                R.string.brushing_quiz_screen_1_answer_2_hint
                            )
                        )
                        put(
                            2,
                            QuizAnswer(
                                R.string.brushing_quiz_screen_1_answer_3,
                                R.string.brushing_quiz_screen_1_answer_3_hint
                            )
                        )
                    },
                    answer = QuizAnswer(
                        R.string.brushing_quiz_screen_1_answer_1,
                        R.string.brushing_quiz_screen_1_answer_1_hint
                    )
                )
            )

            put(
                1, QuizScreen(
                    title = R.string.brushing_quiz_screen_2_title,
                    questionsMap = mutableMapOf<Int, QuizAnswer>().apply {
                        put(
                            0,
                            QuizAnswer(
                                R.string.brushing_quiz_screen_2_answer_1,
                                R.string.brushing_quiz_screen_2_answer_1_hint
                            )
                        )
                        put(
                            1,
                            QuizAnswer(
                                R.string.brushing_quiz_screen_2_answer_2,
                                R.string.brushing_quiz_screen_2_answer_2_hint
                            )
                        )
                        put(
                            2,
                            QuizAnswer(
                                R.string.brushing_quiz_screen_2_answer_3,
                                R.string.brushing_quiz_screen_2_answer_3_hint
                            )
                        )
                    },
                    answer = QuizAnswer(
                        R.string.brushing_quiz_screen_1_answer_1,
                        R.string.brushing_quiz_screen_1_answer_1_hint
                    )
                )
            )

            put(
                2, QuizScreen(
                    title = R.string.brushing_quiz_screen_2_title,
                    questionsMap = mutableMapOf<Int, QuizAnswer>().apply {
                        put(
                            0,
                            QuizAnswer(
                                R.string.brushing_quiz_screen_2_answer_1,
                                R.string.brushing_quiz_screen_2_answer_1_hint
                            )
                        )
                        put(
                            1,
                            QuizAnswer(
                                R.string.brushing_quiz_screen_2_answer_2,
                                R.string.brushing_quiz_screen_2_answer_2_hint
                            )
                        )
                        put(
                            2,
                            QuizAnswer(
                                R.string.brushing_quiz_screen_2_answer_3,
                                R.string.brushing_quiz_screen_2_answer_3_hint
                            )
                        )
                    },
                    answer = QuizAnswer(
                        R.string.brushing_quiz_screen_1_answer_1,
                        R.string.brushing_quiz_screen_1_answer_1_hint
                    )
                )
            )

            put(
                3, QuizScreen(
                    title = R.string.brushing_quiz_screen_2_title,
                    questionsMap = mutableMapOf<Int, QuizAnswer>().apply {
                        put(
                            0,
                            QuizAnswer(
                                R.string.brushing_quiz_screen_2_answer_1,
                                R.string.brushing_quiz_screen_2_answer_1_hint
                            )
                        )
                        put(
                            1,
                            QuizAnswer(
                                R.string.brushing_quiz_screen_2_answer_2,
                                R.string.brushing_quiz_screen_2_answer_2_hint
                            )
                        )
                        put(
                            2,
                            QuizAnswer(
                                R.string.brushing_quiz_screen_2_answer_3,
                                R.string.brushing_quiz_screen_2_answer_3_hint
                            )
                        )
                    },
                    answer = QuizAnswer(
                        R.string.brushing_quiz_screen_1_answer_1,
                        R.string.brushing_quiz_screen_1_answer_1
                    )
                )
            )
        }
        )

        assertEquals(3, quiz.nextScreenIndex)
    }

    /*
    sortedScreens
     */
    @Test
    fun `sortedScreens returns screens sorted by key`() {
        val expectedFirstScreen = QuizScreen(
            title = R.string.brushing_quiz_screen_1_title,
            questionsMap = mutableMapOf<Int, QuizAnswer>().apply {
                put(
                    0,
                    QuizAnswer(
                        R.string.brushing_quiz_screen_1_answer_1,
                        R.string.brushing_quiz_screen_1_answer_1_hint
                    )
                )
                put(
                    1,
                    QuizAnswer(
                        R.string.brushing_quiz_screen_1_answer_2,
                        R.string.brushing_quiz_screen_1_answer_2_hint
                    )
                )
                put(
                    2,
                    QuizAnswer(
                        R.string.brushing_quiz_screen_1_answer_3,
                        R.string.brushing_quiz_screen_1_answer_3_hint
                    )
                )
            },
            answer = QuizAnswer(
                R.string.brushing_quiz_screen_1_answer_1,
                R.string.brushing_quiz_screen_1_answer_1_hint
            )
        )

        val expectedSecondScreen = QuizScreen(
            title = R.string.brushing_quiz_screen_2_title,
            questionsMap = mutableMapOf<Int, QuizAnswer>().apply {
                put(
                    0,
                    QuizAnswer(
                        R.string.brushing_quiz_screen_2_answer_1,
                        R.string.brushing_quiz_screen_2_answer_1_hint
                    )
                )
                put(
                    1,
                    QuizAnswer(
                        R.string.brushing_quiz_screen_2_answer_2,
                        R.string.brushing_quiz_screen_2_answer_2_hint
                    )
                )
                put(
                    2,
                    QuizAnswer(
                        R.string.brushing_quiz_screen_2_answer_3,
                        R.string.brushing_quiz_screen_2_answer_3_hint
                    )
                )
            }
        )

        val expectedThirdScreen = QuizScreen(
            title = R.string.brushing_quiz_screen_2_title,
            questionsMap = mutableMapOf<Int, QuizAnswer>().apply {
                put(
                    0,
                    QuizAnswer(
                        R.string.brushing_quiz_screen_2_answer_1,
                        R.string.brushing_quiz_screen_2_answer_1_hint
                    )
                )
                put(
                    1,
                    QuizAnswer(
                        R.string.brushing_quiz_screen_2_answer_2,
                        R.string.brushing_quiz_screen_2_answer_2_hint
                    )
                )
                put(
                    2,
                    QuizAnswer(
                        R.string.brushing_quiz_screen_2_answer_3,
                        R.string.brushing_quiz_screen_2_answer_3_hint
                    )
                )
            }
        )

        quiz = Quiz(mutableMapOf<Int, QuizScreen>().apply {
            put(0, expectedFirstScreen)

            put(2, expectedThirdScreen)

            put(1, expectedSecondScreen)
        }
        )

        assertEquals(
            listOf(expectedFirstScreen, expectedSecondScreen, expectedThirdScreen),
            quiz.sortedScreens
        )
    }

    /*
    selectedBrushingMode
     */
    @Test
    fun `selectedBrushingMode returns null if last screen doesn't have answer`() {
        assertNull(defaultQuiz().selectedBrushingMode())
    }

    @Test
    fun `selectedBrushingMode does not return null if last question has an anser`() {
        quiz = quizWithAnswerIndexToLastQuestion()

        assertNotNull(quiz.selectedBrushingMode())
    }

    @Test
    fun `selectedBrushingMode returns BrushingMode Strong if selected answer is index 0`() {
        quiz = quizWithAnswerIndexToLastQuestion(answerIndex = 0)

        assertEquals(BrushingMode.Strong, quiz.selectedBrushingMode())
    }

    @Test
    fun `selectedBrushingMode returns BrushingMode Slow if selected answer is index 1`() {
        quiz = quizWithAnswerIndexToLastQuestion(answerIndex = 1)

        assertEquals(BrushingMode.Regular, quiz.selectedBrushingMode())
    }

    @Test
    fun `selectedBrushingMode returns BrushingMode Regular if selected answer is index 2`() {
        quiz = quizWithAnswerIndexToLastQuestion(answerIndex = 2)

        assertEquals(BrushingMode.Slow, quiz.selectedBrushingMode())
    }

    /*
    UTILS
     */

    private fun quizWithAnswerIndexToLastQuestion(answerIndex: Int = 0): Quiz {
        val lastScreenAnswers = mutableMapOf<Int, QuizAnswer>().apply {
            put(
                0,
                QuizAnswer(
                    R.string.brushing_quiz_screen_3_answer_1,
                    R.string.brushing_quiz_screen_3_answer_1_hint
                )
            )
            put(
                1,
                QuizAnswer(
                    R.string.brushing_quiz_screen_3_answer_2,
                    R.string.brushing_quiz_screen_3_answer_2_hint
                )
            )
            put(
                2,
                QuizAnswer(
                    R.string.brushing_quiz_screen_3_answer_3,
                    R.string.brushing_quiz_screen_3_answer_3_hint
                )
            )
        }

        val selectedAnswer = lastScreenAnswers.getValue(answerIndex).copy(selected = true)

        return Quiz(mutableMapOf<Int, QuizScreen>().apply {
            put(
                0, QuizScreen(
                    title = R.string.brushing_quiz_screen_1_title,
                    questionsMap = mutableMapOf<Int, QuizAnswer>().apply {
                        put(
                            0,
                            QuizAnswer(
                                R.string.brushing_quiz_screen_1_answer_1,
                                R.string.brushing_quiz_screen_1_answer_1_hint
                            )
                        )
                        put(
                            1,
                            QuizAnswer(
                                R.string.brushing_quiz_screen_1_answer_2,
                                R.string.brushing_quiz_screen_1_answer_2_hint
                            )
                        )
                        put(
                            2,
                            QuizAnswer(
                                R.string.brushing_quiz_screen_1_answer_3,
                                R.string.brushing_quiz_screen_1_answer_3_hint
                            )
                        )
                    }
                )
            )

            put(1, QuizScreen(
                title = R.string.brushing_quiz_screen_2_title,
                questionsMap = mutableMapOf<Int, QuizAnswer>().apply {
                    put(
                        0,
                        QuizAnswer(
                            R.string.brushing_quiz_screen_2_answer_1,
                            R.string.brushing_quiz_screen_2_answer_1_hint
                        )
                    )
                    put(
                        1,
                        QuizAnswer(
                            R.string.brushing_quiz_screen_2_answer_2,
                            R.string.brushing_quiz_screen_2_answer_2_hint
                        )
                    )
                    put(
                        2,
                        QuizAnswer(
                            R.string.brushing_quiz_screen_2_answer_3,
                            R.string.brushing_quiz_screen_2_answer_3_hint
                        )
                    )
                }
            ))

            put(
                2, QuizScreen(
                    title = R.string.brushing_quiz_screen_3_title,
                    questionsMap = lastScreenAnswers,
                    answer = selectedAnswer
                )
            )
        }
        )
    }
    private fun defaultQuiz() = Quiz(QuizScreenProvider.quizScreenMap)
}

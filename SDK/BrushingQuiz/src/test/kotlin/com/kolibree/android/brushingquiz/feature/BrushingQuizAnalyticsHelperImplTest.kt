/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.feature

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.brushingquiz.feature.BrushingQuizAnalyticsHelperImpl.Companion.EVENT_QUESTION_1_ANSWER_1
import com.kolibree.android.brushingquiz.feature.BrushingQuizAnalyticsHelperImpl.Companion.EVENT_QUESTION_1_ANSWER_2
import com.kolibree.android.brushingquiz.feature.BrushingQuizAnalyticsHelperImpl.Companion.EVENT_QUESTION_1_ANSWER_3
import com.kolibree.android.brushingquiz.feature.BrushingQuizAnalyticsHelperImpl.Companion.EVENT_QUESTION_2_ANSWER_1
import com.kolibree.android.brushingquiz.feature.BrushingQuizAnalyticsHelperImpl.Companion.EVENT_QUESTION_2_ANSWER_2
import com.kolibree.android.brushingquiz.feature.BrushingQuizAnalyticsHelperImpl.Companion.EVENT_QUESTION_2_ANSWER_3
import com.kolibree.android.brushingquiz.feature.BrushingQuizAnalyticsHelperImpl.Companion.EVENT_QUESTION_3_ANSWER_1
import com.kolibree.android.brushingquiz.feature.BrushingQuizAnalyticsHelperImpl.Companion.EVENT_QUESTION_3_ANSWER_2
import com.kolibree.android.brushingquiz.feature.BrushingQuizAnalyticsHelperImpl.Companion.EVENT_QUESTION_3_ANSWER_3
import com.kolibree.android.brushingquiz.feature.BrushingQuizAnalyticsHelperImpl.Companion.EVENT_RESULT_CONFIRM
import com.kolibree.android.brushingquiz.feature.BrushingQuizAnalyticsHelperImpl.Companion.EVENT_RESULT_REVERT
import com.kolibree.android.brushingquiz.feature.BrushingQuizAnalyticsHelperImpl.Companion.EVENT_RESULT_TRY
import com.kolibree.android.brushingquiz.feature.BrushingQuizAnalyticsHelperImpl.Companion.SCREEN_NAME_QUESTION_1
import com.kolibree.android.brushingquiz.feature.BrushingQuizAnalyticsHelperImpl.Companion.SCREEN_NAME_QUESTION_2
import com.kolibree.android.brushingquiz.feature.BrushingQuizAnalyticsHelperImpl.Companion.SCREEN_NAME_QUESTION_3
import com.kolibree.android.brushingquiz.feature.BrushingQuizAnalyticsHelperImpl.Companion.SCREEN_NAME_QUIZ_RESULT
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.EventTracker
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/** [BrushingQuizAnalyticsHelperImpl] tests */
class BrushingQuizAnalyticsHelperImplTest : BaseUnitTest() {

    private val tracker = mock<EventTracker>()

    private lateinit var helper: BrushingQuizAnalyticsHelperImpl

    @Before
    fun before() {
        helper = BrushingQuizAnalyticsHelperImpl(tracker)
    }

    /*
    getScreenNameForQuizQuestionIndex
     */

    @Test
    fun `getScreenNameForQuizQuestionIndex returns SCREEN_NAME_QUESTION_1 for page index 0`() {
        assertEquals(SCREEN_NAME_QUESTION_1, helper.getScreenNameForQuizQuestionIndex(0))
    }

    @Test
    fun `getScreenNameForQuizQuestionIndex returns SCREEN_NAME_QUESTION_2 for page index 1`() {
        assertEquals(SCREEN_NAME_QUESTION_2, helper.getScreenNameForQuizQuestionIndex(1))
    }

    @Test
    fun `getScreenNameForQuizQuestionIndex returns SCREEN_NAME_QUESTION_3 for page index 2`() {
        assertEquals(SCREEN_NAME_QUESTION_3, helper.getScreenNameForQuizQuestionIndex(2))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `getScreenNameForQuizQuestionIndex throws IllegalArgumentException for bad page indexes`() {
        helper.getScreenNameForQuizQuestionIndex(3)
    }

    /*
    getScreenNameForQuizResult
     */

    @Test
    fun `getScreenNameForQuizResult returns SCREEN_NAME_QUIZ_RESULT`() {
        assertEquals(SCREEN_NAME_QUIZ_RESULT, helper.getScreenNameForQuizResult())
    }

    /*
    onQuestionAnswered
     */

    @Test
    fun `onQuestionAnswered question index 0 answer index 0 sends EVENT_QUESTION_1_ANSWER_1 event`() {
        helper.onQuestionAnswered(0, 0)
        verify(tracker).sendEvent(EVENT_QUESTION_1_ANSWER_1)
    }

    @Test
    fun `onQuestionAnswered question index 0 answer index 1 sends EVENT_QUESTION_1_ANSWER_2 event`() {
        helper.onQuestionAnswered(0, 1)
        verify(tracker).sendEvent(EVENT_QUESTION_1_ANSWER_2)
    }

    @Test
    fun `onQuestionAnswered question index 0 answer index 2 sends EVENT_QUESTION_1_ANSWER_3 event`() {
        helper.onQuestionAnswered(0, 2)
        verify(tracker).sendEvent(EVENT_QUESTION_1_ANSWER_3)
    }

    @Test
    fun `onQuestionAnswered question index 1 answer index 0 sends EVENT_QUESTION_2_ANSWER_1 event`() {
        helper.onQuestionAnswered(1, 0)
        verify(tracker).sendEvent(EVENT_QUESTION_2_ANSWER_1)
    }

    @Test
    fun `onQuestionAnswered question index 1 answer index 1 sends EVENT_QUESTION_2_ANSWER_2 event`() {
        helper.onQuestionAnswered(1, 1)
        verify(tracker).sendEvent(EVENT_QUESTION_2_ANSWER_2)
    }

    @Test
    fun `onQuestionAnswered question index 1 answer index 2 sends EVENT_QUESTION_2_ANSWER_3 event`() {
        helper.onQuestionAnswered(1, 2)
        verify(tracker).sendEvent(EVENT_QUESTION_2_ANSWER_3)
    }

    @Test
    fun `onQuestionAnswered question index 2 answer index 0 sends EVENT_QUESTION_3_ANSWER_1 event`() {
        helper.onQuestionAnswered(2, 0)
        verify(tracker).sendEvent(EVENT_QUESTION_3_ANSWER_1)
    }

    @Test
    fun `onQuestionAnswered question index 2 answer index 1 sends EVENT_QUESTION_3_ANSWER_2 event`() {
        helper.onQuestionAnswered(2, 1)
        verify(tracker).sendEvent(EVENT_QUESTION_3_ANSWER_2)
    }

    @Test
    fun `onQuestionAnswered question index 2 answer index 2 sends EVENT_QUESTION_3_ANSWER_3 event`() {
        helper.onQuestionAnswered(2, 2)
        verify(tracker).sendEvent(EVENT_QUESTION_3_ANSWER_3)
    }

    @Test(expected = AssertionError::class)
    fun `onQuestionAnswered question index out of range throws AssertionError`() {
        helper.onQuestionAnswered(3, 1)
    }

    @Test(expected = AssertionError::class)
    fun `onQuestionAnswered answer index out of range throws AssertionError`() {
        helper.onQuestionAnswered(2, -1)
    }

    /*
    onConfirmButtonClick
     */

    @Test
    fun `onConfirmButtonClick sends EVENT_RESULT_CONFIRM event`() {
        helper.onConfirmButtonClick()
        verify(tracker).sendEvent(EVENT_RESULT_CONFIRM)
    }

    /*
    onRevertButtonClick
     */

    @Test
    fun `onRevertButtonClick sends EVENT_RESULT_REVERT event`() {
        helper.onRevertButtonClick()
        verify(tracker).sendEvent(EVENT_RESULT_REVERT)
    }

    /*
    onTryButtonClick
     */

    @Test
    fun `onTryButtonClick sends EVENT_RESULT_TRY event`() {
        helper.onTryButtonClick()
        verify(tracker).sendEvent(EVENT_RESULT_TRY)
    }

    /*
    onGoBackFromProgram
     */

    @Test
    fun `onGoBackFromProgram sends event GoBack with program index`() {
        helper.onGoBackFromProgram(0)
        verify(tracker).sendEvent(AnalyticsEvent("BrushProg1_GoBack"))

        helper.onGoBackFromProgram(1)
        verify(tracker).sendEvent(AnalyticsEvent("BrushProg2_GoBack"))

        helper.onGoBackFromProgram(2)
        verify(tracker).sendEvent(AnalyticsEvent("BrushProg3_GoBack"))
    }

    /*
    Companion
     */

    @Test
    fun `value of SCREEN_NAME_QUESTION_1 is BrushingProgram_Quiz1`() {
        assertEquals("BrushingProgram_Quiz1", SCREEN_NAME_QUESTION_1.name)
    }

    @Test
    fun `value of SCREEN_NAME_QUESTION_2 is BrushingProgram_Quiz2`() {
        assertEquals("BrushingProgram_Quiz2", SCREEN_NAME_QUESTION_2.name)
    }

    @Test
    fun `value of SCREEN_NAME_QUESTION_3 is BrushingProgram_Quiz3`() {
        assertEquals("BrushingProgram_Quiz3", SCREEN_NAME_QUESTION_3.name)
    }

    @Test
    fun `value of SCREEN_NAME_QUIZ_RESULT is BrushingProgram_Quiz1`() {
        assertEquals("BrushingProgram_QuizResult", SCREEN_NAME_QUIZ_RESULT.name)
    }

    @Test
    fun `value of EVENT_QUESTION_1_ANSWER_1 is BrushingProgram_Quiz1_Option1`() {
        assertEquals("BrushingProgram_Quiz1_Option1", EVENT_QUESTION_1_ANSWER_1.name)
    }

    @Test
    fun `value of EVENT_QUESTION_1_ANSWER_2 is BrushingProgram_Quiz1_Option2`() {
        assertEquals("BrushingProgram_Quiz1_Option2", EVENT_QUESTION_1_ANSWER_2.name)
    }

    @Test
    fun `value of EVENT_QUESTION_1_ANSWER_3 is BrushingProgram_Quiz1_Option3`() {
        assertEquals("BrushingProgram_Quiz1_Option3", EVENT_QUESTION_1_ANSWER_3.name)
    }

    @Test
    fun `value of EVENT_QUESTION_2_ANSWER_1 is BrushingProgram_Quiz2_Option1`() {
        assertEquals("BrushingProgram_Quiz2_Option1", EVENT_QUESTION_2_ANSWER_1.name)
    }

    @Test
    fun `value of EVENT_QUESTION_2_ANSWER_2 is BrushingProgram_Quiz2_Option2`() {
        assertEquals("BrushingProgram_Quiz2_Option2", EVENT_QUESTION_2_ANSWER_2.name)
    }

    @Test
    fun `value of EVENT_QUESTION_2_ANSWER_3 is BrushingProgram_Quiz2_Option3`() {
        assertEquals("BrushingProgram_Quiz2_Option3", EVENT_QUESTION_2_ANSWER_3.name)
    }

    @Test
    fun `value of EVENT_QUESTION_3_ANSWER_1 is BrushingProgram_Quiz3_Option1`() {
        assertEquals("BrushingProgram_Quiz3_Option1", EVENT_QUESTION_3_ANSWER_1.name)
    }

    @Test
    fun `value of EVENT_QUESTION_3_ANSWER_2 is BrushingProgram_Quiz3_Option2`() {
        assertEquals("BrushingProgram_Quiz3_Option2", EVENT_QUESTION_3_ANSWER_2.name)
    }

    @Test
    fun `value of EVENT_QUESTION_3_ANSWER_3 is BrushingProgram_Quiz3_Option3`() {
        assertEquals("BrushingProgram_Quiz3_Option3", EVENT_QUESTION_3_ANSWER_3.name)
    }

    @Test
    fun `value of EVENT_RESULT_CONFIRM is BrushingProgram_QuizResult_Confirm`() {
        assertEquals("BrushingProgram_QuizResult_Confirm", EVENT_RESULT_CONFIRM.name)
    }

    @Test
    fun `value of EVENT_RESULT_REVERT is BrushingProgram_QuizResult_Revert`() {
        assertEquals("BrushingProgram_QuizResult_Revert", EVENT_RESULT_REVERT.name)
    }

    @Test
    fun `value of EVENT_RESULT_TRY is BrushingProgram_QuizResult_Try`() {
        assertEquals("BrushingProgram_QuizResult_Try", EVENT_RESULT_TRY.name)
    }
}

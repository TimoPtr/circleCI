/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.question

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDay
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestionCardViewStateTest : BaseUnitTest() {

    private lateinit var viewState: QuestionCardViewState

    @Test
    fun `show points when question not answered`() {
        val mockQuestion = QuestionOfTheDay(
            id = 1,
            question = "This is mock question",
            points = 12,
            answers = listOf(QuestionOfTheDay.Answer(1, "Answer", correct = true))
        )

        viewState = QuestionCardViewState(DynamicCardPosition.ZERO, true, mockQuestion)

        assertTrue(viewState.pointsVisible)
    }

    @Test
    fun `hide points when question not available`() {
        viewState = QuestionCardViewState(DynamicCardPosition.ZERO, true, null)

        assertFalse(viewState.pointsVisible)
    }
}

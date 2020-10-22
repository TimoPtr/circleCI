/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.presentation.quiz

import com.kolibree.android.app.test.BaseUnitTest
import org.junit.Assert.assertEquals
import org.junit.Test

class BrushingQuizViewStateTest : BaseUnitTest() {
    private lateinit var viewState: QuizViewState

    /*
    init
     */
    @Test
    fun `initial state sets current position to 0`() {
        viewState = QuizViewState.initial()

        assertEquals(0, viewState.currentQuizPosition)
    }

    /*
    withCurrentQuizPosition
     */
    @Test
    fun `withCurrentQuizPosition returns instance with updated index`() {
        viewState = QuizViewState.initial()

        val newIndex = 30

        assertEquals(QuizViewState(newIndex), viewState.withCurrentQuizPosition(newIndex))
    }
}

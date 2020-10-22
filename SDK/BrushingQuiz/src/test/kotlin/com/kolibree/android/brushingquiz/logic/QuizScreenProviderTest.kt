/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.logic

import com.kolibree.android.app.test.BaseUnitTest
import junit.framework.TestCase.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizScreenProviderTest : BaseUnitTest() {
    @Test
    fun `QuizScreenProvider returns non-empty map`() {
        assertTrue(QuizScreenProvider.provideScreens().isNotEmpty())
    }

    @Test
    fun `QuizScreenProvider returns list without negative order`() {
        val screens = QuizScreenProvider.provideScreens()

        assertFalse(screens.keys.any { it < 0 })
    }

    @Test
    fun `QuizScreenProvider returns list with successive order and 0 indexed`() {
        val screens = QuizScreenProvider.provideScreens()

        @Suppress("ReplaceManualRangeWithIndicesCalls")
        (0 until screens.size).forEach { order ->
            screens.keys.single { it == order }
        }
    }

    @Test
    fun `QuizScreenProvider returns QuizScreen with titles`() {
        val screens = QuizScreenProvider.provideScreens()

        assertFalse(screens.values.any { it.title == 0 })
    }

    /*
    QuizScreens
     */

    @Test
    fun `QuizScreenProvider returns QuizScreens without answer`() {
        val screens = QuizScreenProvider.provideScreens()

        assertFalse(screens.values.any { it.answer != null })
    }

    @Test
    fun `QuizScreenProvider returns QuizScreens with non-empty questions`() {
        QuizScreenProvider.provideScreens().forEach {
            assertTrue(it.value.sortedQuestions.isNotEmpty())
        }
    }

    @Test
    fun `QuizScreenProvider returns QuizScreens with QuizQuestions with message`() {
        QuizScreenProvider.provideScreens().forEach {
            assertFalse(it.value.sortedQuestions.any { it.message == 0 })
        }
    }
}

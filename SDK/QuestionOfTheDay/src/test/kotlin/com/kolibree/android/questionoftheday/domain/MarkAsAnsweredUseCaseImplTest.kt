/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.questionoftheday.domain

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.questionoftheday.data.repo.QuestionRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

class MarkAsAnsweredUseCaseImplTest : BaseUnitTest() {

    private val repository: QuestionRepository = mock()

    private val useCase = MarkAsAnsweredUseCaseImpl(repository)

    @Test
    fun `markAsAnswered call the repository with the right param`() {
        val expectedId: Long = 123
        val questionOfTheDay = mock<QuestionOfTheDay>()
        whenever(questionOfTheDay.id).thenReturn(expectedId)

        useCase.markAsAnswered(questionOfTheDay)

        verify(repository).markAsAnswered(expectedId)
    }
}

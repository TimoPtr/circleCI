/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.questionoftheday.domain

import com.kolibree.android.questionoftheday.data.repo.QuestionOfTheDayRepository
import io.reactivex.Completable
import javax.inject.Inject

internal interface MarkAsAnsweredUseCase {

    fun markAsAnswered(questionOfTheDay: QuestionOfTheDay): Completable
}

internal class MarkAsAnsweredUseCaseImpl @Inject constructor(
    private val repository: QuestionOfTheDayRepository
) : MarkAsAnsweredUseCase {

    override fun markAsAnswered(questionOfTheDay: QuestionOfTheDay): Completable =
        repository.markAsAnswered(questionOfTheDay.id)
}

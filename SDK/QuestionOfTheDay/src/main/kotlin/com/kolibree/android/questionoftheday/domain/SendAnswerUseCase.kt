/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.questionoftheday.domain

import com.kolibree.android.network.api.ApiError
import com.kolibree.android.network.api.ApiErrorCode
import com.kolibree.android.questionoftheday.data.repo.QuestionOfTheDayRepository
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import org.threeten.bp.OffsetDateTime

internal interface SendAnswerUseCase {

    fun sendAnswer(
        questionOfTheDay: QuestionOfTheDay,
        answer: QuestionOfTheDay.Answer,
        answerTime: OffsetDateTime
    ): Completable
}

internal class AlreadyAnsweredException(message: String?) : Exception(message)

internal class SendAnswerUseCaseImpl(
    private val scheduler: Scheduler,
    private val repository: QuestionOfTheDayRepository,
    private val markAsAnsweredUseCase: MarkAsAnsweredUseCase
) : SendAnswerUseCase {

    @Inject
    constructor(
        repository: QuestionOfTheDayRepository,
        markAsAnsweredUseCase: MarkAsAnsweredUseCase
    ) : this(
        scheduler = Schedulers.io(),
        repository = repository,
        markAsAnsweredUseCase = markAsAnsweredUseCase
    )

    override fun sendAnswer(
        questionOfTheDay: QuestionOfTheDay,
        answer: QuestionOfTheDay.Answer,
        answerTime: OffsetDateTime
    ): Completable {
        val networkRequest = sendAnswerCompletable(questionOfTheDay, answer, answerTime)
        return Completable.mergeDelayError(listOf(minDelay(), networkRequest))
    }

    private fun sendAnswerCompletable(
        questionOfTheDay: QuestionOfTheDay,
        answer: QuestionOfTheDay.Answer,
        answerTime: OffsetDateTime
    ): Completable {
        return repository.sendAnswer(questionOfTheDay, answer, answerTime)
            .handleError(questionOfTheDay)
            .andThen(markAsAnswered(questionOfTheDay))
    }

    private fun Completable.handleError(questionOfTheDay: QuestionOfTheDay): Completable {
        return onErrorResumeNext { error ->
            if (error is ApiError && isErrorAlreadyAnswered(error)) {
                markAsAnswered(questionOfTheDay).andThen(
                    Completable.error(AlreadyAnsweredException(error.displayableMessage))
                )
            } else {
                Completable.error(error)
            }
        }
    }

    private fun isErrorAlreadyAnswered(error: ApiError) =
        error.internalErrorCode == ApiErrorCode.QUESTION_ALREADY_ANSWERED

    private fun markAsAnswered(questionOfTheDay: QuestionOfTheDay) = Completable.defer {
        markAsAnsweredUseCase.markAsAnswered(questionOfTheDay)
    }

    /**
     * Ensures that network request takes minimum [MIN_REQUEST_TIME] seconds
     */
    private fun minDelay() = Completable.timer(MIN_REQUEST_TIME, TimeUnit.SECONDS, scheduler)

    companion object {
        const val MIN_REQUEST_TIME = 2L
    }
}

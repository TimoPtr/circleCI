/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.dagger

import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.questionoftheday.data.repo.QuestionOfTheDayRepository
import com.kolibree.android.questionoftheday.di.QuestionOfTheDayCoreModule
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDay
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayStatus
import dagger.Module
import dagger.Provides
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.processors.ReplayProcessor
import org.threeten.bp.OffsetDateTime

@Module(includes = [QuestionOfTheDayCoreModule::class])
internal class EspressoQuestionOfTheDayModule {

    @AppScope
    @Provides
    fun providesFakeRepository(): FakeQuestionOfTheDayRepository = FakeQuestionOfTheDayRepository()

    @Provides
    fun providesRepository(
        fakeRepository: FakeQuestionOfTheDayRepository
    ): QuestionOfTheDayRepository = fakeRepository
}

class FakeQuestionOfTheDayRepository : QuestionOfTheDayRepository {

    private val items: ReplayProcessor<QuestionOfTheDayStatus> = ReplayProcessor.create()

    fun mock(questionOfTheDayStatus: QuestionOfTheDayStatus) {
        items.onNext(questionOfTheDayStatus)
    }

    override fun fetchQuestion(profileId: Long): Completable {
        return Completable.complete()
    }

    override fun getQuestionFlowable(profileId: Long): Flowable<QuestionOfTheDayStatus> {
        return items
    }

    override fun getQuestionStatus(profileId: Long): Single<QuestionOfTheDayStatus> {
        return items.take(1).singleOrError()
    }

    override fun sendAnswer(
        questionOfTheDay: QuestionOfTheDay,
        answer: QuestionOfTheDay.Answer,
        answerTime: OffsetDateTime
    ): Completable {
        return Completable.complete()
    }

    override fun markAsAnswered(questionId: Long): Completable {
        return Completable.complete()
    }
}

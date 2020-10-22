/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.questionoftheday.data.repo

import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.failearly.FailEarly.overrideDelegateWith
import com.kolibree.android.questionoftheday.data.api.QuestionApi
import com.kolibree.android.questionoftheday.data.api.model.request.AnswerApiResponse
import com.kolibree.android.questionoftheday.data.api.model.request.QuestionApiResponse
import com.kolibree.android.questionoftheday.data.api.model.request.UserResponse
import com.kolibree.android.questionoftheday.data.room.dao.QuestionDao
import com.kolibree.android.questionoftheday.data.room.dao.model.AnswerEntity
import com.kolibree.android.questionoftheday.data.room.dao.model.QuestionEntity
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDay
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDay.Answer
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayStatus.Available
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayStatus.NotAvailable
import com.kolibree.android.synchronizator.Synchronizator
import com.kolibree.android.test.extensions.setFixedDate
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import org.junit.Test
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset
import retrofit2.Response

class QuestionRepositoryTest : BaseUnitTest() {

    private val synchronizator: Synchronizator = mock()
    private val accountDatastore: AccountDatastore = mock()
    private val questionDao: QuestionDao = mock()
    private val questionApi: QuestionApi = mock()

    private lateinit var repository: QuestionRepository

    override fun setup() {
        repository = QuestionRepository(
            questionApi,
            questionDao,
            accountDatastore,
            synchronizator
        )
    }

    @Test
    fun `fetchQuestion insert and delete questions`() {
        val accountId: Long = 123
        val ownerProfileId: Long = 456
        val questionId: Long = 7
        val questionText = "abc"
        val response = Response.success(
            QuestionApiResponse(
                listOf(
                    AnswerApiResponse(1, "1"),
                    AnswerApiResponse(2, "2"),
                    AnswerApiResponse(3, "3")
                ), correct = 2, id = questionId, text = questionText, userResponse = null
            )
        )

        TrustedClock.setFixedDate(OffsetDateTime.of(2020, 1, 21, 8, 10, 43, 0, ZoneOffset.UTC))
        whenever(accountDatastore.getAccountMaybe()).thenReturn(
            Maybe.just(getAccountInternal(accountId, ownerProfileId))
        )
        whenever(questionApi.fetchQuestion(eq(accountId), eq(ownerProfileId), any()))
            .thenReturn(Single.just(response))

        val testCompletable = repository.fetchQuestion(ownerProfileId).test()

        inOrder(questionDao) {
            this.verify(questionDao).insertQuestionWithAnswers(
                QuestionEntity(
                    questionId,
                    ownerProfileId,
                    questionText,
                    false,
                    1579665600,
                    ZoneOffset.UTC
                ),
                listOf(
                    AnswerEntity(1, questionId, ownerProfileId, "1", false),
                    AnswerEntity(2, questionId, ownerProfileId, "2", true),
                    AnswerEntity(3, questionId, ownerProfileId, "3", false)
                )
            )
        }

        testCompletable.assertComplete()
    }

    @Test
    fun `fetchQuestion insert an already answered response`() {
        val accountId: Long = 123
        val ownerProfileId: Long = 456
        val questionId: Long = 7
        val questionText = "abc"
        val response = Response.success(
            QuestionApiResponse(
                listOf(
                    AnswerApiResponse(1, "1"),
                    AnswerApiResponse(2, "2"),
                    AnswerApiResponse(3, "3")
                ), correct = 2, id = questionId, text = questionText,
                userResponse = UserResponse(1, 1, TrustedClock.getNowOffsetDateTime())
            )
        )

        TrustedClock.setFixedDate(OffsetDateTime.of(2020, 1, 21, 8, 10, 43, 0, ZoneOffset.UTC))
        whenever(accountDatastore.getAccountMaybe()).thenReturn(
            Maybe.just(getAccountInternal(accountId, ownerProfileId))
        )
        whenever(questionApi.fetchQuestion(eq(accountId), eq(ownerProfileId), any()))
            .thenReturn(Single.just(response))

        val testCompletable = repository.fetchQuestion(ownerProfileId).test()

        inOrder(questionDao) {
            this.verify(questionDao).insertQuestionWithAnswers(
                QuestionEntity(
                    questionId,
                    ownerProfileId,
                    questionText,
                    true,
                    1579665600,
                    ZoneOffset.UTC
                ),
                listOf(
                    AnswerEntity(1, questionId, ownerProfileId, "1", false),
                    AnswerEntity(2, questionId, ownerProfileId, "2", true),
                    AnswerEntity(3, questionId, ownerProfileId, "3", false)
                )
            )
        }

        testCompletable.assertComplete()
    }

    private fun getAccountInternal(
        accountId: Long,
        ownerProfileId: Long
    ) = AccountInternal(id = accountId, ownerProfileId = ownerProfileId)

    @Test
    fun `getQuestionFlowable returns a correctly mapped question status according to the profile id`() {

        TrustedClock.setFixedDate(OffsetDateTime.of(2020, 1, 20, 8, 10, 43, 0, ZoneOffset.UTC))

        val questionText = "text"
        val questionId: Long = 123
        val profileId: Long = 789
        val answers = listOf(
            AnswerEntity(1, questionId, profileId, "A", false),
            AnswerEntity(2, questionId, profileId, "B", true),
            AnswerEntity(3, questionId, profileId, "C", false)
        )

        whenever(accountDatastore.getAccountMaybe()).thenReturn(
            Maybe.just(getAccountInternal(0, profileId))
        )
        whenever(questionDao.getQuestionFlowable(profileId)).thenReturn(
            Flowable.just(
                QuestionEntity(
                    questionId, profileId, questionText, false, 1579665600, ZoneOffset.UTC
                ) to answers
            )
        )

        val testFlowable = repository.getQuestionFlowable(profileId).test()

        testFlowable.assertValue(
            Available(
                QuestionOfTheDay(
                    questionId, questionText, 1, listOf(
                        Answer(1, "A"),
                        Answer(2, "B", true),
                        Answer(3, "C")
                    )
                )
            )
        )
    }

    @Test
    fun `getQuestionStatus returns a single of the last question status`() {

        val questionText = "text"
        val questionId: Long = 123
        val profileId: Long = 789

        TrustedClock.setFixedDate(OffsetDateTime.of(2020, 1, 20, 8, 10, 43, 0, ZoneOffset.UTC))

        whenever(accountDatastore.getAccountMaybe()).thenReturn(
            Maybe.just(getAccountInternal(0, profileId))
        )
        whenever(questionDao.getQuestionSingle(profileId)).thenReturn(
            Single.just(
                listOf(
                    QuestionEntity(
                        questionId, profileId, questionText, false, 1579665600, ZoneOffset.UTC
                    ) to listOf(AnswerEntity(1, questionId, profileId, "ABC", true))
                )
            )
        )

        val testSingle = repository.getQuestionStatus(profileId).test()

        testSingle.assertValue(
            Available(QuestionOfTheDay(questionId, questionText, 1, listOf(Answer(1, "ABC", true))))
        )
    }

    @Test
    fun `getQuestionStatus returns a single of NotAvailable if there is no question in the db`() {
        val profileId: Long = 123

        whenever(accountDatastore.getAccountMaybe()).thenReturn(
            Maybe.just(getAccountInternal(0, profileId))
        )
        whenever(questionDao.getQuestionSingle(profileId)).thenReturn(Single.just(emptyList()))

        val testSingle = repository.getQuestionStatus(profileId).test()

        testSingle.assertValue(NotAvailable)
    }

    @Test
    fun `getQuestionStatus throw a fail early if there is more than one question returned`() {
        overrideDelegateWith(TestDelegate)
        val profileId: Long = 123

        whenever(accountDatastore.getAccountMaybe()).thenReturn(
            Maybe.just(getAccountInternal(0, profileId))
        )
        whenever(questionDao.getQuestionSingle(profileId)).thenReturn(
            Single.just(listOf(mock(), mock()))
        )

        val testSingle = repository.getQuestionStatus(profileId).test()

        testSingle.assertError(AssertionError::class.java)
    }

    @Test
    fun `two different account can share different subset of questions and answers`() {

        val firstQuestionText = "firstText"
        val firstQuestionId: Long = 111
        val firstProfileId: Long = 222

        val secondQuestionText = "secondText"
        val secondQuestionId: Long = 333
        val secondProfileId: Long = 444

        TrustedClock.setFixedDate(OffsetDateTime.of(2020, 1, 20, 8, 10, 43, 0, ZoneOffset.UTC))

        whenever(questionDao.getQuestionSingle(firstProfileId)).thenReturn(
            Single.just(
                listOf(
                    QuestionEntity(
                        firstQuestionId, firstProfileId, firstQuestionText,
                        false, 1579665600, ZoneOffset.UTC
                    ) to listOf(
                        AnswerEntity(78, firstQuestionId, firstProfileId, "firstAnswer", true)
                    )
                )
            )
        )

        whenever(questionDao.getQuestionSingle(secondProfileId)).thenReturn(
            Single.just(
                listOf(
                    QuestionEntity(
                        secondQuestionId, secondProfileId,
                        secondQuestionText, false, 1579665600, ZoneOffset.UTC
                    ) to listOf(
                        AnswerEntity(42, secondQuestionId, secondProfileId, "secondAnswer", true)
                    )
                )
            )
        )

        whenever(accountDatastore.getAccountMaybe()).thenReturn(
            Maybe.just(getAccountInternal(0, firstProfileId))
        )

        repository.getQuestionStatus(firstProfileId).test().assertValue(
            Available(
                QuestionOfTheDay(
                    firstQuestionId, firstQuestionText,
                    1, listOf(Answer(78, "firstAnswer", true))
                )
            )
        )

        whenever(accountDatastore.getAccountMaybe()).thenReturn(
            Maybe.just(getAccountInternal(0, secondProfileId))
        )

        repository.getQuestionStatus(secondProfileId).test().assertValue(
            Available(
                QuestionOfTheDay(
                    secondQuestionId, secondQuestionText,
                    1, listOf(Answer(42, "secondAnswer", true))
                )
            )
        )
    }

    @Test
    fun `markAsAnswered call the dao update method`() {
        val questionId: Long = 10
        val profileId: Long = 30

        whenever(questionDao.updateAnswered(any(), any())).thenReturn(Completable.complete())
        whenever(accountDatastore.getAccountMaybe()).thenReturn(
            Maybe.just(getAccountInternal(4, profileId))
        )

        repository.markAsAnswered(questionId)
            .test()
            .assertComplete()

        verify(questionDao).updateAnswered(questionId, profileId)
    }
}

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
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.ZONE_FORMATTER
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.network.toParsedResponseCompletable
import com.kolibree.android.network.toParsedResponseSingle
import com.kolibree.android.questionoftheday.QuestionOfTheDayStatusMapper
import com.kolibree.android.questionoftheday.data.QuestionApiMapper
import com.kolibree.android.questionoftheday.data.api.QuestionApi
import com.kolibree.android.questionoftheday.data.api.model.request.AnswerQuestionRequest
import com.kolibree.android.questionoftheday.data.api.model.request.QuestionApiResponse
import com.kolibree.android.questionoftheday.data.room.dao.QuestionDao
import com.kolibree.android.questionoftheday.data.room.dao.model.AnswerEntity
import com.kolibree.android.questionoftheday.data.room.dao.model.QuestionEntity
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDay
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDay.Answer
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayStatus
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayStatus.Expired
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayStatus.NotAvailable
import com.kolibree.android.synchronizator.Synchronizator
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import javax.inject.Inject
import okhttp3.ResponseBody
import org.threeten.bp.OffsetDateTime
import retrofit2.Response

@VisibleForApp
interface QuestionOfTheDayRepository {

    /**
     * Fetch the questions for given [profileId] from the Back-end
     * and update the database when needed
     */
    fun fetchQuestion(profileId: Long): Completable

    /**
     * Returns a Flowable of [QuestionOfTheDayStatus] aggregated with their respective answers, the
     * streams is updated when a new Question is inserted
     */
    fun getQuestionFlowable(profileId: Long): Flowable<QuestionOfTheDayStatus>

    /**
     * Returns the most recent [QuestionOfTheDayStatus] without the Answers aggregation,
     * if there is no questions the [QuestionOfTheDayStatus] is [Expired]
     */
    fun getQuestionStatus(profileId: Long): Single<QuestionOfTheDayStatus>

    fun sendAnswer(
        questionOfTheDay: QuestionOfTheDay,
        answer: Answer,
        answerTime: OffsetDateTime
    ): Completable

    fun markAsAnswered(questionId: Long): Completable
}

internal class QuestionRepository @Inject constructor(
    private val questionApi: QuestionApi,
    private val questionDao: QuestionDao,
    private val accountDatastore: AccountDatastore,
    private val synchronizator: Synchronizator
) : QuestionOfTheDayRepository {

    override fun fetchQuestion(profileId: Long): Completable {
        return accountDatastore.getAccountMaybe()
            .flatMapSingle { fetchQuestion(it, profileId) }
            .map(QuestionApiMapper)
            .flatMapCompletable(::insertQuestions)
    }

    override fun getQuestionFlowable(profileId: Long): Flowable<QuestionOfTheDayStatus> {
        return questionDao.getQuestionFlowable(profileId)
            .map(QuestionOfTheDayStatusMapper)
    }

    override fun getQuestionStatus(profileId: Long): Single<QuestionOfTheDayStatus> {
        return getQuestionForProfile(profileId)
    }

    override fun markAsAnswered(questionId: Long): Completable {
        return accountDatastore.getAccountMaybe()
            .flatMapCompletable { account ->
                questionDao.updateAnswered(questionId, account.ownerProfileId)
            }
    }

    private fun getQuestionForProfile(profileId: Long): Single<QuestionOfTheDayStatus> {
        return questionDao.getQuestionSingle(profileId)
            .map {

                FailEarly.failInConditionMet(
                    it.size > 1,
                    "Questions size should be 0 or 1, be sure to delete the questions" +
                        "before inserting new one"
                )

                it.firstOrNull()?.let { entities ->
                    QuestionOfTheDayStatusMapper(entities)
                } ?: NotAvailable
            }
    }

    private fun fetchQuestion(
        account: AccountInternal,
        profileId: Long
    ): Single<Pair<QuestionApiResponse, Long>> {
        val timezone = ZONE_FORMATTER.format(TrustedClock.getNowOffsetDateTime())

        return questionApi.fetchQuestion(
            accountId = account.id,
            profileId = profileId,
            timezone = timezone
        ).toParsedResponseSingle().map {
            it to profileId
        }
    }

    override fun sendAnswer(
        questionOfTheDay: QuestionOfTheDay,
        answer: Answer,
        answerTime: OffsetDateTime
    ): Completable {
        return accountDatastore
            .getAccountMaybe()
            .flatMapSingle { account -> sendAnswer(account, questionOfTheDay, answer, answerTime) }
            .toParsedResponseCompletable()
            .andThen(synchronizator.synchronizeCompletable())
    }

    private fun sendAnswer(
        account: AccountInternal,
        questionOfTheDay: QuestionOfTheDay,
        answer: Answer,
        answerTime: OffsetDateTime
    ): Single<Response<ResponseBody>> {
        return questionApi.sendAnswer(
            accountId = account.id,
            profileId = account.currentProfileId ?: error("Unable to get current profile id!"),
            body = AnswerQuestionRequest(
                id = questionOfTheDay.id,
                answerId = answer.id,
                answeredAt = answerTime
            )
        )
    }

    private fun insertQuestions(questionAnswerPair: Pair<QuestionEntity, List<AnswerEntity>>) =
        Completable.fromCallable {
            questionDao.insertQuestionWithAnswers(
                questionAnswerPair.first,
                questionAnswerPair.second
            )
        }
}

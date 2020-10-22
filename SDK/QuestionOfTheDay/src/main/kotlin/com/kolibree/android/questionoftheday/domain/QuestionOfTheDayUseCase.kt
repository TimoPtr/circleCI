/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.questionoftheday.domain

import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.questionoftheday.data.repo.QuestionOfTheDayRepository
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayStatus.Expired
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayStatus.NotAvailable
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import javax.inject.Inject

@VisibleForApp
interface QuestionOfTheDayUseCase {

    /**
     * Provides question for current profile for specified day. It can be either
     * - [QuestionOfTheDayStatus.Available]
     * - [QuestionOfTheDayStatus.AlreadyAnswered]
     * - [QuestionOfTheDayStatus.NotAvailable]
     * - [QuestionOfTheDayStatus.Expired]
     *
     * Even though we have a QuestionOfTheDayJobService, it is not guaranteed to have
     * the most recent Question. This methods check the last question status, and if the
     * question is expired or not available, then it retrieve the new one.
     */
    fun questionStatusStream(): Flowable<QuestionOfTheDayStatus>

    /**
     * Refreshes questions for all profiles.
     * If the question is out of date or unavailable, it tries to get a new one from BE.
     */
    fun refreshQuestions(): Completable
}

internal class QuestionOfTheDayUseCaseImpl @Inject constructor(
    private val accountDatastore: AccountDatastore,
    private val profileProvider: CurrentProfileProvider,
    private val repository: QuestionOfTheDayRepository
) : QuestionOfTheDayUseCase {

    override fun questionStatusStream(): Flowable<QuestionOfTheDayStatus> {
        return profileProvider
            .currentProfileFlowable()
            .switchMap { profile ->
                fetchQuestionIfNeeded(profile.id)
                    .andThen(repository.getQuestionFlowable(profile.id))
            }
    }

    override fun refreshQuestions(): Completable {
        return accountDatastore
            .getAccountMaybe()
            .flatMapObservable { account ->
                val profileIds = account.internalProfiles.map { it.id }
                Observable.fromIterable(profileIds)
            }
            .flatMapCompletable(::fetchQuestionIfNeeded)
    }

    private fun fetchQuestionIfNeeded(profileId: Long): Completable {
        return repository.getQuestionStatus(profileId)
            .flatMapCompletable { status ->
                if (status is Expired || status is NotAvailable) {
                    repository.fetchQuestion(profileId)
                } else {
                    Completable.complete()
                }
            }
    }
}

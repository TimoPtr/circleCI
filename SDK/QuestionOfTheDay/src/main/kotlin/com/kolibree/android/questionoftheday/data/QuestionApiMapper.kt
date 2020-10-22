/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.questionoftheday.data

import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.clock.TrustedClock.systemZoneOffset
import com.kolibree.android.extensions.atStartOfKolibreeDay
import com.kolibree.android.questionoftheday.data.api.model.request.QuestionApiResponse
import com.kolibree.android.questionoftheday.data.room.dao.model.AnswerEntity
import com.kolibree.android.questionoftheday.data.room.dao.model.QuestionEntity
import org.threeten.bp.OffsetDateTime

/**
 * Convert a [QuestionApiResponse] to it's given Database Representation [QuestionEntity]
 */
internal object QuestionApiMapper :
        (Pair<QuestionApiResponse, Long>) -> Pair<QuestionEntity, List<AnswerEntity>> {
    override fun invoke(params: Pair<QuestionApiResponse, Long>): Pair<QuestionEntity, List<AnswerEntity>> {

        val apiResponse = params.first
        val profileId = params.second

        return QuestionEntity(
            questionId = apiResponse.id,
            questionProfileId = profileId,
            questionText = apiResponse.text,
            questionAnswered = apiResponse.userResponse != null,
            questionExpirationZoneOffset = systemZoneOffset,
            questionExpirationTimestamp = getExpirationDate().toEpochSecond()
        ) to apiResponse.mapAnswers(profileId)
    }

    private fun QuestionApiResponse.mapAnswers(profileId: Long): List<AnswerEntity> {
        return answers.map { answerApiResponse ->
            AnswerEntity(
                answerId = answerApiResponse.id,
                questionId = id,
                questionProfileId = profileId,
                answerText = answerApiResponse.text,
                isCorrect = correct == answerApiResponse.id
            )
        }
    }

    /**
     * Get the expiration date according to the current Kolibree day
     * If the current date is before a new Kolibree Day, then the expiration date is today
     * Else if we already are in a new Kolibree day, then it returns tomorrow as the expiration date
     */
    private fun getExpirationDate(): OffsetDateTime {
        val currentDate = TrustedClock.getNowOffsetDateTime()
        val kolibreeDayDate = currentDate.toLocalDate().atStartOfKolibreeDay()

        return if (currentDate.isBefore(kolibreeDayDate)) {
            kolibreeDayDate
        } else {
            kolibreeDayDate.plusDays(1)
        }
    }
}

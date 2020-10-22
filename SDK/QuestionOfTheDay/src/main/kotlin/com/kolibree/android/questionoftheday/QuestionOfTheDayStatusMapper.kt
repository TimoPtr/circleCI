/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.questionoftheday

import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.questionoftheday.data.room.dao.model.AnswerEntity
import com.kolibree.android.questionoftheday.data.room.dao.model.QuestionEntity
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDay
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDay.Answer
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayStatus
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayStatus.AlreadyAnswered
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayStatus.Available
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayStatus.Expired

/**
 * Map a [QuestionEntity] and its related [AnswerEntity] list from the data layer
 * to their model Representation [QuestionOfTheDayStatus] & [Answer] from the domain layer
 */
internal object QuestionOfTheDayStatusMapper :
        (Pair<QuestionEntity, List<AnswerEntity>>) -> QuestionOfTheDayStatus {

    override fun invoke(entities: Pair<QuestionEntity, List<AnswerEntity>>): QuestionOfTheDayStatus {
        val questionEntity = entities.first

        return when {
            questionEntity.isExpired() -> Expired
            questionEntity.questionAnswered -> AlreadyAnswered
            else -> Available(
                QuestionOfTheDay(
                    id = questionEntity.questionId,
                    question = questionEntity.questionText,
                    answers = mapAnswers(entities.second),
                    points = AWARDED_POINTS
                )
            )
        }
    }

    private fun mapAnswers(answerEntities: List<AnswerEntity>): List<Answer> {
        return answerEntities.map { answerEntity ->
            Answer(
                id = answerEntity.answerId,
                text = answerEntity.answerText,
                correct = answerEntity.isCorrect
            )
        }
    }

    private fun QuestionEntity.isExpired() =
        TrustedClock.getNowOffsetDateTime().isAfter(expirationDate)
}

const val AWARDED_POINTS = 1

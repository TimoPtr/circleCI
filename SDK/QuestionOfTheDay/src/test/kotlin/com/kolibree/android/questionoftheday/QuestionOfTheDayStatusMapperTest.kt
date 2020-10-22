/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.questionoftheday

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.questionoftheday.data.room.dao.model.AnswerEntity
import com.kolibree.android.questionoftheday.data.room.dao.model.QuestionEntity
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDay
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDay.Answer
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayStatus.AlreadyAnswered
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayStatus.Available
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayStatus.Expired
import com.kolibree.android.test.extensions.setFixedDate
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset

class QuestionOfTheDayStatusMapperTest : BaseUnitTest() {

    @Test
    fun `QuestionOfTheDayMapper send the status containing the right question`() {

        val currentDate = getCurrentDate()
        val expirationDate = getCurrentDate().plusHours(1)
        val expirationTimestamp = expirationDate.toEpochSecond()
        val expirationZoneOffset = expirationDate.offset
        val questionId = 234L
        val questionProfileId = 333L
        val questionText = "blabla"
        val questionEntity = QuestionEntity(
            questionId, questionProfileId, questionText, false,
            expirationTimestamp, expirationZoneOffset
        )
        val answers = listOf(
            AnswerEntity(1, questionId, questionProfileId, "A", true)
        )

        TrustedClock.setFixedDate(currentDate)

        val status = QuestionOfTheDayStatusMapper(questionEntity to answers)

        assertEquals(
            Available(
                QuestionOfTheDay(
                    questionId, questionText, 1, listOf(Answer(1, "A", true))
                )
            ), status
        )
    }

    @Test
    fun `QuestionOfTheDayMapper send expired status if the entity has expired`() {
        val questionEntity = getExpiredQuestion()

        val status = QuestionOfTheDayStatusMapper(questionEntity to mock())

        assertEquals(Expired, status)
    }

    @Test
    fun `QuestionOfTheDayMapper send already answered status if the entity is answered`() {

        val currentDate = getCurrentDate()
        val expirationDate = getCurrentDate().plusHours(1)
        val expirationTimestamp = expirationDate.toEpochSecond()
        val expirationZoneOffset = expirationDate.offset
        val questionEntity = QuestionEntity(
            1, 1, "test", true,
            expirationTimestamp, expirationZoneOffset
        )
        TrustedClock.setFixedDate(currentDate)

        val status = QuestionOfTheDayStatusMapper(questionEntity to mock())

        assertEquals(AlreadyAnswered, status)
    }

    @Test
    fun `QuestionOfTheDayMapper should returns Expired status in priority even if it has been answered`() {
        val questionEntity = getExpiredQuestion().copy(questionAnswered = true)

        val status = QuestionOfTheDayStatusMapper(questionEntity to mock())

        assertEquals(Expired, status)
    }

    private fun getExpiredQuestion(): QuestionEntity {
        val expirationDate = getCurrentDate().minusHours(1)
        val expirationTimestamp = expirationDate.toEpochSecond()
        val expirationZoneOffset = expirationDate.offset
        return QuestionEntity(
            1, 2, "blabla", false,
            expirationTimestamp, expirationZoneOffset
        )
    }

    private fun getCurrentDate(): OffsetDateTime {
        return OffsetDateTime.of(
            2020, 1, 21, 8, 10, 43, 0, ZoneOffset.UTC
        )
    }
}

/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.questionoftheday.data

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.questionoftheday.data.api.model.request.AnswerApiResponse
import com.kolibree.android.questionoftheday.data.api.model.request.QuestionApiResponse
import com.kolibree.android.questionoftheday.data.api.model.request.UserResponse
import com.kolibree.android.questionoftheday.data.room.dao.model.AnswerEntity
import com.kolibree.android.questionoftheday.data.room.dao.model.QuestionEntity
import com.kolibree.android.test.extensions.setFixedDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset

class QuestionApiMapperTest : BaseUnitTest() {

    @Test
    fun `QuestionApiMapper correctly map the response`() {
        val questionText = "abc"
        val questionId: Long = 7
        val questionProfileId: Long = 234
        val answerText1 = "answerABC"
        val answerText2 = "QWERTY"
        val answerText3 = "TOTO"

        val response = getResponse(answerText1, answerText2, answerText3, questionId, questionText)

        TrustedClock.setFixedDate(OffsetDateTime.of(2020, 1, 21, 8, 10, 43, 0, ZoneOffset.UTC))

        val (questionEntity, answers) = QuestionApiMapper(response to questionProfileId)

        assertEquals(
            QuestionEntity(
                questionId = questionId,
                questionProfileId = questionProfileId,
                questionAnswered = false,
                questionText = questionText,
                questionExpirationTimestamp = 1579665600,
                questionExpirationZoneOffset = ZoneOffset.UTC
            ), questionEntity
        )

        assertEquals(
            listOf(
                AnswerEntity(1, questionId, questionProfileId, answerText1, false),
                AnswerEntity(2, questionId, questionProfileId, answerText2, true),
                AnswerEntity(3, questionId, questionProfileId, answerText3, false)
            ), answers
        )
    }

    @Test
    fun `QuestionApiMapper mark the response as answered if userResponse is present`() {
        val questionText = "abc"
        val questionId: Long = 7
        val questionProfileId: Long = 234
        val answerText1 = "answerABC"
        val answerText2 = "QWERTY"
        val answerText3 = "TOTO"

        val response =
            getResponse(answerText1, answerText2, answerText3, questionId, questionText)
                .copy(userResponse = UserResponse(questionId, 1, TrustedClock.getNowOffsetDateTime()))

        val (questionEntity, _) = QuestionApiMapper(response to questionProfileId)

        assertTrue(questionEntity.questionAnswered)
    }

    @Test
    fun `QuestionApiMapper mark the response as not answered if userResponse is not present`() {
        val questionText = "abc"
        val questionId: Long = 7
        val questionProfileId: Long = 234
        val answerText1 = "answerABC"
        val answerText2 = "QWERTY"
        val answerText3 = "TOTO"

        val response = getResponse(answerText1, answerText2, answerText3, questionId, questionText)

        val (questionEntity, _) = QuestionApiMapper(response to questionProfileId)

        assertFalse(questionEntity.questionAnswered)
    }

    @Test
    fun `if today is 3h59am the question expiration date should be set at 4h00am the same day`() {
        val profileId = 10L
        val hours = 3
        val minutes = 59
        val dayOfMonth = 21
        val todayDate = OffsetDateTime.of(
            2020, 1, dayOfMonth, hours,
            minutes, 0, 0, ZoneOffset.UTC
        )

        TrustedClock.setFixedDate(todayDate)

        val (questionEntity, _) = QuestionApiMapper(getResponse() to profileId)

        val expirationDate = questionEntity.expirationDate

        assertEquals(4, expirationDate.hour)
        assertEquals(0, expirationDate.minute)
        assertEquals(dayOfMonth, expirationDate.dayOfMonth)
    }

    @Test
    fun `if today is 4h01am the question expiration date should be set tomorrow at 4h00`() {
        val profileId: Long = 234
        val hours = 4
        val minutes = 1
        val dayOfMonth = 21
        val expectedExpirationDay = 22
        val todayDate = OffsetDateTime.of(
            2020, 1, dayOfMonth, hours,
            minutes, 0, 0, ZoneOffset.UTC
        )

        TrustedClock.setFixedDate(todayDate)

        val (questionEntity, _) = QuestionApiMapper(getResponse() to profileId)

        val expirationDate = questionEntity.expirationDate

        assertEquals(4, expirationDate.hour)
        assertEquals(0, expirationDate.minute)
        assertEquals(expectedExpirationDay, expirationDate.dayOfMonth)
    }

    private fun getResponse(
        answerText1: String = "",
        answerText2: String = "",
        answerText3: String = "",
        questionId: Long = 1,
        questionText: String = ""
    ): QuestionApiResponse {
        return QuestionApiResponse(
            listOf(
                AnswerApiResponse(1, answerText1),
                AnswerApiResponse(2, answerText2),
                AnswerApiResponse(3, answerText3)
            ), correct = 2, id = questionId, text = questionText, userResponse = null
        )
    }
}

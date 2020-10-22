/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.questionoftheday.data.room.dao

import android.content.Context
import androidx.room.Room.inMemoryDatabaseBuilder
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.hum.questionoftheday.data.room.QuestionRoomAppDatabase
import com.kolibree.android.questionoftheday.data.room.dao.model.AnswerEntity
import com.kolibree.android.questionoftheday.data.room.dao.model.QuestionEntity
import com.kolibree.android.test.BaseInstrumentationTest
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.ZoneOffset

@RunWith(AndroidJUnit4::class)
class QuestionDaoTest : BaseInstrumentationTest() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var database: QuestionRoomAppDatabase
    private lateinit var questionDao: QuestionDao

    override fun setUp() {
        super.setUp()
        database = inMemoryDatabaseBuilder(context(), QuestionRoomAppDatabase::class.java).build()
        questionDao = database.questionDao()
    }

    override fun tearDown() {
        super.tearDown()

        database.close()
    }

    @Test
    fun getQuestion_has_no_values_on_initial_state() {
        val profileId: Long = 456
        questionDao.getQuestionFlowable(profileId)
            .test()
            .assertNoErrors()
            .assertNoValues()
    }

    @Test
    fun getQuestion_returns_expected_values_after_insert() {
        val profileId: Long = 456
        val questionId: Long = 123
        val questionEntity = getQuestionEntity(questionId, profileId)
        val answers = getAnswerEntities(questionId, profileId)

        questionDao.insertQuestionWithAnswers(questionEntity, answers)

        questionDao.getQuestionFlowable(profileId)
            .test()
            .assertNoErrors()
            .assertValue(questionEntity to answers)
    }

    @Test
    fun insertQuestionWithAnswers_should_delete_the_previous_question_for_same_profile() {
        val questionId: Long = 456
        val profileId: Long = 789
        val firstQuestion = getQuestionEntity(questionId, profileId)
        val firstAnswers = getAnswerEntities(questionId, profileId)

        questionDao.insertQuestionWithAnswers(firstQuestion, firstAnswers)

        questionDao.getQuestionSingle(profileId)
            .test()
            .assertValueCount(1)
            .assertValue { it.size == 1 }
            .assertValue { it.first() == firstQuestion to firstAnswers }

        val secondQuestion = getQuestionEntity(questionId, profileId)
        val secondAnswers = getAnswerEntities(questionId, profileId)

        questionDao.insertQuestionWithAnswers(secondQuestion, secondAnswers)

        questionDao.getQuestionSingle(profileId)
            .test()
            .assertValueCount(1)
            .assertValue { it.size == 1 }
            .assertValue { it.first() == secondQuestion to secondAnswers }
    }

    @Test
    fun insertQuestionWithAnswers_should_not_delete_question_for_another_profile() {
        val firstQuestionId: Long = 456
        val firstProfileId: Long = 789
        val firstQuestion = getQuestionEntity(firstQuestionId, firstProfileId)
        val firstAnswers = getAnswerEntities(firstQuestionId, firstProfileId)

        val secondQuestionId: Long = 456
        val secondProfileId: Long = 789
        val secondQuestion = getQuestionEntity(secondQuestionId, secondProfileId)
        val secondAnswers = getAnswerEntities(secondQuestionId, secondProfileId)

        questionDao.insertQuestionWithAnswers(firstQuestion, firstAnswers)
        questionDao.insertQuestionWithAnswers(secondQuestion, secondAnswers)

        questionDao.getQuestionSingle(firstProfileId)
            .test()
            .assertValueCount(1)
            .assertValue { it.size == 1 }
            .assertValue { it.first() == firstQuestion to firstAnswers }

        questionDao.getQuestionSingle(secondProfileId)
            .test()
            .assertValueCount(1)
            .assertValue { it.size == 1 }
            .assertValue { it.first() == secondQuestion to secondAnswers }
    }

    @Test
    fun getQuestionFlowable_should_dispatch_new_values() {
        val questionId: Long = 789
        val profileId: Long = 456
        val firstQuestion = getQuestionEntity(questionId, profileId)
        val firstAnswers = getAnswerEntities(questionId, profileId)

        questionDao.insertQuestionWithAnswers(firstQuestion, firstAnswers)

        val questionFlowable = questionDao.getQuestionFlowable(profileId).test()

        questionFlowable
            .assertNoErrors()
            .assertValueCount(1)
            .assertValueAt(0, firstQuestion to firstAnswers)

        val secondQuestion = getQuestionEntity(questionId, profileId)
        val secondAnswers = getAnswerEntities(questionId, profileId)

        questionDao.insertQuestionWithAnswers(secondQuestion, secondAnswers)

        questionFlowable
            .assertNoErrors()
            .assertValueCount(2)
            .assertValueAt(1, secondQuestion to secondAnswers)
    }

    @Test
    fun getQuestionFlowable_should_dispatch_new_value_when_an_answer_is_updated() {
        val questionId: Long = 789
        val profileId: Long = 456
        val firstQuestion = getQuestionEntity(questionId, profileId)
        val firstAnswers = getAnswerEntities(questionId, profileId)
        val expectedModelUpdate = firstQuestion.copy(questionAnswered = true)

        questionDao.insertQuestionWithAnswers(firstQuestion, firstAnswers)

        val questionFlowable = questionDao.getQuestionFlowable(profileId).test()

        questionFlowable
            .assertNoErrors()
            .assertValueCount(1)
            .assertValueAt(0, firstQuestion to firstAnswers)

        questionDao.updateAnswered(questionId, profileId)
            .test()
            .assertComplete()

        questionFlowable
            .assertNoErrors()
            .assertValueCount(2)
            .assertValueAt(1, expectedModelUpdate to firstAnswers)
    }

    private fun getQuestionEntity(questionId: Long, profileId: Long): QuestionEntity {
        return QuestionEntity(questionId, profileId, "toto", false, 1234, ZoneOffset.UTC)
    }

    private fun getAnswerEntities(questionId: Long, profileId: Long): List<AnswerEntity> {
        return listOf(
            AnswerEntity(1, questionId, profileId, "Answer1", false),
            AnswerEntity(2, questionId, profileId, "Answer2", true),
            AnswerEntity(3, questionId, profileId, "Answer3", false)
        )
    }
}

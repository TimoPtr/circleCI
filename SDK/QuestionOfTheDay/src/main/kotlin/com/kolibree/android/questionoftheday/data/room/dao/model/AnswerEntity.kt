/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.questionoftheday.data.room.dao.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.kolibree.android.questionoftheday.data.room.dao.model.AnswerEntity.Companion.FIELD_ANSWER_PROFILE_ID
import com.kolibree.android.questionoftheday.data.room.dao.model.AnswerEntity.Companion.FIELD_ANSWER_QUESTION_ID
import com.kolibree.android.questionoftheday.data.room.dao.model.QuestionEntity.Companion.FIELD_QUESTION_ID
import com.kolibree.android.questionoftheday.data.room.dao.model.QuestionEntity.Companion.FIELD_QUESTION_PROFILE_ID

@Entity(
    tableName = "answer",
    foreignKeys = [ForeignKey(
        entity = QuestionEntity::class,
        parentColumns = [FIELD_QUESTION_ID, FIELD_QUESTION_PROFILE_ID],
        childColumns = [FIELD_ANSWER_QUESTION_ID, FIELD_ANSWER_PROFILE_ID],
        onDelete = ForeignKey.CASCADE
    )]
)
internal data class AnswerEntity(
    @ColumnInfo(name = FIELD_ANSWER_ID, index = true) @PrimaryKey val answerId: Long,
    @ColumnInfo(name = FIELD_ANSWER_QUESTION_ID, index = true) val questionId: Long,
    @ColumnInfo(name = FIELD_ANSWER_PROFILE_ID, index = true) val questionProfileId: Long,
    @ColumnInfo(name = FIELD_ANSWER_TEXT) val answerText: String,
    @ColumnInfo(name = FIELD_ANSWER_IS_CORRECT) val isCorrect: Boolean
) {

    companion object {
        const val FIELD_ANSWER_ID = "answer_id"
        const val FIELD_ANSWER_PROFILE_ID = "answer_profile_id"
        const val FIELD_ANSWER_TEXT = "answer_text"
        const val FIELD_ANSWER_IS_CORRECT = "answer_is_correct"
        const val FIELD_ANSWER_QUESTION_ID = "answer_question_id"
    }
}

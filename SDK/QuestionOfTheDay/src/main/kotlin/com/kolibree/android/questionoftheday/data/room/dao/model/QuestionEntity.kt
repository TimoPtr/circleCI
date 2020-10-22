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
import androidx.room.TypeConverters
import com.kolibree.android.questionoftheday.data.room.dao.model.QuestionEntity.Companion.FIELD_QUESTION_ID
import com.kolibree.android.questionoftheday.data.room.dao.model.QuestionEntity.Companion.FIELD_QUESTION_PROFILE_ID
import com.kolibree.android.room.ZoneOffsetConverter
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset

@Entity(
    tableName = "question_of_the_day",
    primaryKeys = [FIELD_QUESTION_ID, FIELD_QUESTION_PROFILE_ID]
)
@TypeConverters(ZoneOffsetConverter::class)
internal data class QuestionEntity constructor(
    @ColumnInfo(name = FIELD_QUESTION_ID, index = true) val questionId: Long,
    @ColumnInfo(name = FIELD_QUESTION_PROFILE_ID, index = true) val questionProfileId: Long,
    @ColumnInfo(name = FIELD_QUESTION_TEXT) val questionText: String,
    @ColumnInfo(name = FIELD_QUESTION_ANSWERED) val questionAnswered: Boolean,
    @ColumnInfo(name = FIELD_EXPIRATION_TIMESTAMP) val questionExpirationTimestamp: Long,
    @ColumnInfo(name = FIELD_EXPIRATION_ZONE_OFFSET) val questionExpirationZoneOffset: ZoneOffset
) {

    /**
     * Create the OffsetDateTime object from the fields store in the DB (timestamp and ZoneOffset)
     */
    val expirationDate: OffsetDateTime
        get() = OffsetDateTime.ofInstant(
            Instant.ofEpochSecond(questionExpirationTimestamp),
            questionExpirationZoneOffset
        )

    internal companion object {
        const val FIELD_QUESTION_ID = "question_id"
        const val FIELD_QUESTION_PROFILE_ID = "question_profile_id"
        const val FIELD_QUESTION_TEXT = "question_text"
        const val FIELD_QUESTION_ANSWERED = "question_answered"
        const val FIELD_EXPIRATION_TIMESTAMP = "question_expiration_timestamp"
        const val FIELD_EXPIRATION_ZONE_OFFSET = "question_expiration_zone_offset"
    }
}

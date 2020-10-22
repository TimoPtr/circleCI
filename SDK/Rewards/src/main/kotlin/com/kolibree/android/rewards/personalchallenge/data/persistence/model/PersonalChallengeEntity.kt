/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personalchallenge.data.persistence.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.kolibree.android.rewards.personalchallenge.data.mapper.levelFromStringedValue
import com.kolibree.android.rewards.personalchallenge.data.mapper.objectiveFromJsonString
import com.kolibree.android.rewards.personalchallenge.data.mapper.periodFromStringedData
import com.kolibree.android.rewards.personalchallenge.domain.model.V1PersonalChallenge
import com.kolibree.android.room.DateConvertersString
import com.kolibree.android.synchronizator.data.database.UuidConverters
import java.util.UUID
import org.threeten.bp.ZonedDateTime

@Keep
@Entity(tableName = "personal_challenges")
@TypeConverters(DateConvertersString::class, UuidConverters::class)
internal data class PersonalChallengeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val backendId: Long?,
    val profileId: Long,
    val objectiveType: String,
    val difficultyLevel: String,
    val duration: Long,
    val durationUnit: String,
    val creationDate: ZonedDateTime,
    val updateDate: ZonedDateTime,
    val completionDate: ZonedDateTime?,
    val progress: Int,
    val uuid: UUID? = null
) {

    fun toV1Challenge() = V1PersonalChallenge(
        objectiveType = objectiveFromJsonString(objectiveType),
        difficultyLevel = levelFromStringedValue(difficultyLevel),
        period = periodFromStringedData(duration, durationUnit),
        creationDate = creationDate,
        completionDate = completionDate,
        progress = progress
    )
}

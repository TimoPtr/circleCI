/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.partnerships.headspace.data.persistence.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.kolibree.android.partnerships.data.persistence.model.PartnershipEntity
import java.lang.IllegalArgumentException

@Entity(tableName = "headspace_partnership")
@TypeConverters(StateConverters::class)
internal data class HeadspacePartnershipEntity(
    @PrimaryKey
    @ColumnInfo(name = "profile_id") override val profileId: Long,
    @ColumnInfo(name = "status") val status: State,
    @ColumnInfo(name = "points_needed", defaultValue = "NULL") val pointsNeeded: Int? = null,
    @ColumnInfo(name = "points_threshold", defaultValue = "NULL") val pointsThreshold: Int? = null,
    @ColumnInfo(name = "discount_code", defaultValue = "NULL") val discountCode: String? = null,
    @ColumnInfo(name = "redeem_url", defaultValue = "NULL") val redeemUrl: String? = null
) : PartnershipEntity {

    internal enum class State(val persistentKey: String) {
        IN_PROGRESS("in_progress"),
        INACTIVE("inactive"),
        UNLOCKED("unlocked");

        companion object {

            fun from(value: String?) = when (value) {
                null -> IN_PROGRESS
                IN_PROGRESS.persistentKey -> IN_PROGRESS
                INACTIVE.persistentKey -> INACTIVE
                UNLOCKED.persistentKey -> UNLOCKED
                else -> throw IllegalArgumentException("Cannot reconstruct State from $value")
            }
        }
    }
}

internal class StateConverters {

    @TypeConverter
    fun setState(value: HeadspacePartnershipEntity.State?): String? =
        value?.persistentKey

    @TypeConverter
    fun getState(value: String?): HeadspacePartnershipEntity.State? =
        HeadspacePartnershipEntity.State.from(value)
}

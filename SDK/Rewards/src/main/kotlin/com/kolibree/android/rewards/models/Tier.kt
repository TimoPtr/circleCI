/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.models

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.kolibree.android.room.DateConvertersString
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDate

/**
 * Interface to be used by non-persistence users
 */
@Keep
interface Tier : Parcelable {
    val level: Int
    val smilesPerBrushing: Int
    val challengesNeeded: Int
    val pictureUrl: String
    val rank: String
}

@Keep
@Entity(tableName = "tiers")
@TypeConverters(DateConvertersString::class)
@Parcelize
internal data class TierEntity(
    @PrimaryKey override val level: Int,
    override val smilesPerBrushing: Int,
    override val challengesNeeded: Int,
    override val pictureUrl: String,
    override val rank: String,
    val creationDate: LocalDate,
    val message: String
) : Tier

@Parcelize
object EmptyTier : Tier {
    override val level: Int = -1
    override val smilesPerBrushing: Int = -1
    override val challengesNeeded: Int = -1
    override val pictureUrl: String = ""
    override val rank: String = ""
}

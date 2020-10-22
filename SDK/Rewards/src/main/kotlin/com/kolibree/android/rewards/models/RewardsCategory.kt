package com.kolibree.android.rewards.models

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Interface to be used by non-persistence consumers
 */
@Keep
interface CategoryWithProgress {
    val categoryName: String
    val challenges: List<ChallengeWithProgress>
}

@Keep
@Entity(tableName = "categories")
internal data class CategoryEntity
@JvmOverloads constructor(
    @SerializedName("category") @PrimaryKey val name: String,
    @Ignore val challenges: List<ChallengeEntity> = listOf()
)

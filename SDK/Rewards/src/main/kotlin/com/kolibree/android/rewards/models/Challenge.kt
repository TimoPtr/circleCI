package com.kolibree.android.rewards.models

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Interface to be used by non-persistence consumers
 */
@Keep
interface Challenge : Parcelable {
    val id: Long
    val name: String
    val category: String
    val greetingMessage: String
    val description: String
    val pictureUrl: String
    val smilesReward: Int
    val action: String?
}

@Entity(
    tableName = "challenges",
    foreignKeys = [ForeignKey(
        entity = CategoryEntity::class,
        parentColumns = arrayOf("name"),
        childColumns = arrayOf("category"),
        onDelete = CASCADE
    )],
    indices = [Index(value = ["category"])]
)
@Keep
@Parcelize
internal data class ChallengeEntity(
    @SerializedName("challenge_id") @PrimaryKey override val id: Long,
    @SerializedName("challenge_name") override val name: String,
    override val greetingMessage: String,
    override val description: String,
    override val pictureUrl: String,
    override val smilesReward: Int,
    override val action: String?,
    @ColumnInfo(name = "category") var internalCategory: String?
) : Challenge {
    override val category: String
        get() = if (internalCategory.isNullOrEmpty()) "" else internalCategory!!
}

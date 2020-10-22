package com.kolibree.sdkws.data.model.gopirate

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.kolibree.sdkws.data.database.contract.GoPirateContract
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

@Keep
@Entity(tableName = "gopirate")
@TypeConverters(GoPirateConverters::class)
data class GoPirateData constructor(
    @PrimaryKey
    @ColumnInfo(name = GoPirateContract.COLUMN_PROFILE_ID)
    val profileId: Int,
    val rank: Int,
    val gold: Int,
    @ColumnInfo(name = GoPirateContract.COLUMN_LAST_WORLD_REACHED)
    val lastWorldReached: Int,
    @ColumnInfo(name = GoPirateContract.COLUMN_LAST_LEVEL_REACHED)
    val lastLevelReached: Int,
    @ColumnInfo(name = GoPirateContract.COLUMN_LAST_LEVEL_BRUSH)
    val lastLevelBrush: Int,
    @ColumnInfo(name = GoPirateContract.COLUMN_LAST_SHIP_BOUGHT)
    val lastShipBought: Int,
    @ColumnInfo(name = GoPirateContract.COLUMN_AVATAR_COLOR)
    val avatarColor: Int,
    @ColumnInfo(name = GoPirateContract.COLUMN_BRUSHING_NUMBER)
    val brushingNumber: Int,
    val treasures: Treasures
) {

    fun update(data: UpdateGoPirateData): GoPirateData {
        for (treasure in data.newTreasures) {
            if (!treasures.contains(treasure)) {
                treasures.add(treasure)
            }
        }

        return this.copy(
            rank = Math.max(rank, data.rank),
            gold = gold + data.gold,
            lastWorldReached = Math.max(lastWorldReached, data.lastWorldReached),
            lastLevelReached = Math.max(lastLevelReached, data.lastLevelReached),
            lastShipBought = data.lastShipBought,
            lastLevelBrush = data.lastLevelBrush,
            avatarColor = data.avatarColor,
            brushingNumber = brushingNumber + if (data.hasBrushing()) 1 else 0,
            treasures = treasures
        )
    }

    companion object {
        private const val FIELD_RANK = "rank"
        private const val FIELD_GOLD = "gold"
        private const val FIELD_LAST_WORLD_REACHED = "last_world_reached"
        private const val FIELD_LAST_LEVEL_REACHED = "last_level_reached"
        private const val FIELD_LAST_LEVEL_BRUSH = "last_level_brush"
        private const val FIELD_LAST_SHIP_BOUGHT = "last_ship_bought"
        private const val FIELD_AVATAR_COLOR = "avatar_color"
        private const val FIELD_TREASURES = "treasures"
        private const val FIELD_BRUSHING_NUMBER = "brushing_number"

        @JvmStatic
        fun fromUpdateGoPirateData(profileId: Long, data: UpdateGoPirateData) = GoPirateData(
            profileId = profileId.toInt(),
            rank = data.rank,
            gold = data.gold,
            lastWorldReached = data.lastWorldReached,
            lastLevelReached = data.lastLevelReached,
            lastLevelBrush = data.lastLevelBrush,
            lastShipBought = data.lastShipBought,
            avatarColor = data.avatarColor,
            treasures = Treasures.fromList(data.newTreasures),
            brushingNumber = data.brushing
        )

        @JvmStatic
        @Throws(JSONException::class)
        fun fromJson(profileId: Long, rawJson: String): GoPirateData {
            val json = JSONObject(rawJson)
            val rank = json.getInt(FIELD_RANK)
            val gold = json.getInt(FIELD_GOLD)
            val lastWorldReached = json.getInt(FIELD_LAST_WORLD_REACHED)
            val lastLevelReached = json.getInt(FIELD_LAST_LEVEL_REACHED)
            val lastLevelBrush = json.getInt(FIELD_LAST_LEVEL_BRUSH)
            val lastShipBought = json.getInt(FIELD_LAST_SHIP_BOUGHT)
            val avatarColor = json.getInt(FIELD_AVATAR_COLOR)
            val brushingNumber = json.getInt(FIELD_BRUSHING_NUMBER)

            val array = json.getJSONObject(FIELD_TREASURES)
            val keys = array.keys()

            val treasures = Treasures()

            @Suppress("TooGenericExceptionCaught")
            while (keys.hasNext()) {
                try {
                    val key = keys.next() as String

                    if (array.getBoolean(key)) {
                        treasures.add(key.toInt())
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }

            return GoPirateData(
                profileId = profileId.toInt(),
                rank = rank,
                gold = gold,
                lastWorldReached = lastWorldReached,
                lastLevelReached = lastLevelReached,
                lastLevelBrush = lastLevelBrush,
                lastShipBought = lastShipBought,
                avatarColor = avatarColor,
                treasures = treasures,
                brushingNumber = brushingNumber
            )
        }
    }
}

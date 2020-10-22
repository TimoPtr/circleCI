/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.persistence

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kolibree.kml.MouthZone16
import com.kolibree.statsoffline.models.YearWeek
import com.kolibree.statsoffline.persistence.models.StatsPlaqueAggregate
import org.json.JSONObject
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth

internal class YearMonthConverters {
    @TypeConverter
    fun setYearMonthTo(value: YearMonth): Long = value.atEndOfMonth().toEpochDay()

    @TypeConverter
    fun getYearMonthFrom(value: Long): YearMonth = YearMonth.from(LocalDate.ofEpochDay(value))
}

internal class YearWeekConverters {
    @TypeConverter
    fun setYearWeekTo(value: YearWeek): String = value.toString()

    @TypeConverter
    fun getYearWeekFrom(value: String): YearWeek = YearWeek.parse(value)
}

internal class MouthZoneCheckupConverter {
    @TypeConverter
    fun fromCheckupMap(zoneCheckupMap: Map<MouthZone16, Float>): String {
        return JSONObject(zoneCheckupMap.mapKeys { entry -> entry.key.toString() }).toString()
    }

    @TypeConverter
    fun toCheckupMap(zoneCheckupMapString: String): Map<MouthZone16, Float> {
        val mapAsJson = JSONObject(zoneCheckupMapString)
        return mapAsJson.keys()
            .asSequence()
            .associate { mouthZone ->
                MouthZone16.valueOf(mouthZone) to mapAsJson.getDouble(mouthZone).toFloat()
            }
    }
}

internal class PlaqueAggregateConverter {
    private companion object {
        val gson = Gson()
    }

    @TypeConverter
    fun fromPlaqueAggregateMap(plaqueAggregate: Map<MouthZone16, StatsPlaqueAggregate>?): String? {
        if (plaqueAggregate == null) return null

        return gson.toJson(plaqueAggregate)
    }

    @TypeConverter
    fun toPlaqueAggregateMap(plaqueAggregateMapString: String?): Map<MouthZone16, StatsPlaqueAggregate>? {
        if (plaqueAggregateMapString == null) return null

        val type = object : TypeToken<Map<MouthZone16, StatsPlaqueAggregate>>() {}.type

        return gson.fromJson<Map<MouthZone16, StatsPlaqueAggregate>>(plaqueAggregateMapString, type)
    }
}

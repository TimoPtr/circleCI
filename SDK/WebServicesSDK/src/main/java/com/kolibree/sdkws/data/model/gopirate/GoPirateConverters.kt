package com.kolibree.sdkws.data.model.gopirate

import androidx.room.TypeConverter

internal class GoPirateConverters {
    companion object {
        private const val SEPARATOR = ","
    }

    @TypeConverter
    fun fromTreasures(rawTreasures: String?): Treasures {
        val treasures = Treasures()
        if (rawTreasures != null && !rawTreasures.isEmpty()) {
            for (s in rawTreasures.split(SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                treasures.add(Integer.parseInt(s.trim()))
            }
        }

        return treasures
    }

    @TypeConverter
    fun toTreasures(treasures: Treasures): String {
        val b = StringBuilder()

        for (i in treasures.indices) {
            b.append(treasures.get(i))

            if (i < treasures.size - 1) {
                b.append(SEPARATOR)
            }
        }

        return b.toString()
    }
}

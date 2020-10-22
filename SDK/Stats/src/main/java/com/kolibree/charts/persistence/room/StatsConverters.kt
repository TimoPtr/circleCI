package com.kolibree.charts.persistence.room

import androidx.room.TypeConverter
import org.threeten.bp.Clock
import org.threeten.bp.ZoneId

/**
 * Created by guillaumeagis on 21/05/2018.
 * Converter used to store Clock object, associated to a stat into Room.
 * In order to do so , we convert the clock to it's zone Id, a string.
 * From this zoneId, we are able to generate the Clock associated to it.
 */

internal class StatsConverters {

    /**
     * Convert a given clock to it's zone ID
     */
    @TypeConverter
    fun fromClock(clock: Clock): String = clock.zone.id

    /**
     * Convert a given zone ID to a clock
     */
    @TypeConverter
    fun toClock(zoneId: String): Clock = Clock.system(ZoneId.of(zoneId))
}

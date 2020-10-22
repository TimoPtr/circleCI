package com.kolibree.android.rewards.persistence

import androidx.room.TypeConverter
import com.kolibree.android.rewards.synchronization.CHALLENGE_PROGRESS_DATETIME_FORMATTER
import com.kolibree.android.rewards.synchronization.SMILES_HISTORY_DATETIME_FORMATTER
import org.threeten.bp.ZonedDateTime

internal class ChallengeProgressZoneDateTimeToStringConverter {
    @TypeConverter
    fun fromZonedDateTime(zonedDateTime: ZonedDateTime?): String? {
        return zonedDateTime?.let { CHALLENGE_PROGRESS_DATETIME_FORMATTER.format(it) }
    }

    @TypeConverter
    fun toZonedDateTime(dateTime: String?): ZonedDateTime? {
        return dateTime?.let { ZonedDateTime.parse(it, CHALLENGE_PROGRESS_DATETIME_FORMATTER) }
    }
}

internal class SmilesHistoryZoneDateTimeToStringConverter {
    @TypeConverter
    fun fromZonedDateTime(zonedDateTime: ZonedDateTime): String {
        return SMILES_HISTORY_DATETIME_FORMATTER.format(zonedDateTime)
    }

    @TypeConverter
    fun toZonedDateTime(dateTime: String): ZonedDateTime {
        return ZonedDateTime.parse(dateTime, SMILES_HISTORY_DATETIME_FORMATTER)
    }
}

internal class SmilesHistoryNullableZoneDateTimeToStringConverter {
    @TypeConverter
    fun fromZonedDateTime(zonedDateTime: ZonedDateTime?): String? {
        return zonedDateTime?.let { SMILES_HISTORY_DATETIME_FORMATTER.format(it) }
    }

    @TypeConverter
    fun toZonedDateTime(dateTime: String?): ZonedDateTime? {
        return dateTime?.let { ZonedDateTime.parse(it, SMILES_HISTORY_DATETIME_FORMATTER) }
    }
}

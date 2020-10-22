package com.kolibree.android.offlinebrushings.sync

import org.threeten.bp.ZonedDateTime

internal interface LastSyncDateFormatter {
    fun parse(textualDate: String): ZonedDateTime
    fun format(date: ZonedDateTime): String
}

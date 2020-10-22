package com.kolibree.android.offlinebrushings.sync

import org.threeten.bp.ZonedDateTime

internal interface LastSyncProvider {
    fun get(tbMac: String): LastSyncData
    fun put(tbMac: String, date: ZonedDateTime)
}

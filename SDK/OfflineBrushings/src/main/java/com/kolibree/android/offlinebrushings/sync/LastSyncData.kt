package com.kolibree.android.offlinebrushings.sync

import androidx.annotation.Keep
import com.kolibree.android.clock.TrustedClock
import org.threeten.bp.ZonedDateTime

@Keep
sealed class LastSyncData(val toothbrushMac: String)

@Keep
data class LastSyncDate(val tbMac: String, val date: ZonedDateTime) : LastSyncData(tbMac) {

    companion object {
        @JvmStatic
        fun now(tbMac: String): LastSyncDate {
            val date = TrustedClock.getNowZonedDateTime()
            return LastSyncDate(tbMac, date)
        }
    }
}

@Keep
data class StartSync(val tbMac: String) : LastSyncData(tbMac)

@Keep
data class NeverSync(val tbMac: String) : LastSyncData(tbMac)

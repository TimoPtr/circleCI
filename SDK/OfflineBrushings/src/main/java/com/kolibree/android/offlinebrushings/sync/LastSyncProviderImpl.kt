package com.kolibree.android.offlinebrushings.sync

import android.content.SharedPreferences
import javax.inject.Inject
import org.threeten.bp.ZonedDateTime

internal class LastSyncProviderImpl
@Inject constructor(
    private val preferences: SharedPreferences,
    private val formatter: LastSyncDateFormatter
) : LastSyncProvider {

    override fun get(tbMac: String): LastSyncData {
        val textualDate = preferences.getString(key(tbMac), "") ?: ""
        if (textualDate.isEmpty()) {
            return NeverSync(tbMac)
        }
        val time = formatter.parse(textualDate)
        return LastSyncDate(tbMac, time)
    }

    override fun put(tbMac: String, date: ZonedDateTime) {
        val textualDate = formatter.format(date)
        preferences.edit()
            .putString(key(tbMac), textualDate)
            .apply()
    }

    companion object {
        val TAG = LastSyncProviderImpl.javaClass.name

        val KEY_TB_MAC = "$TAG.KEY_TB_MAC"
        fun key(tbMac: String) = "$KEY_TB_MAC.$tbMac"
    }
}

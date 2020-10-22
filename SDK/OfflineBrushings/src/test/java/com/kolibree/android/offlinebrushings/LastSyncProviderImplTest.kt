package com.kolibree.android.offlinebrushings

import android.content.SharedPreferences
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.offlinebrushings.sync.LastSyncDate
import com.kolibree.android.offlinebrushings.sync.LastSyncDateFormatter
import com.kolibree.android.offlinebrushings.sync.LastSyncProviderImpl
import com.kolibree.android.offlinebrushings.sync.NeverSync
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito

class LastSyncProviderImplTest : BaseUnitTest() {

    @Mock
    private lateinit var preferences: SharedPreferences

    @Mock
    private lateinit var editor: SharedPreferences.Editor

    @Mock
    private lateinit var formatter: LastSyncDateFormatter

    private lateinit var lastSyncProvider: LastSyncProviderImpl

    override fun setup() {
        super.setup()

        whenever(preferences.edit()).thenReturn(editor)
        lastSyncProvider = Mockito.spy(LastSyncProviderImpl(preferences, formatter))
    }

    @Test
    fun get_invokes_preferencesGetString() {
        val mac = "mA:c1"
        whenever(preferences.getString(any(), any())).thenReturn("")

        lastSyncProvider.get(mac)

        val key = LastSyncProviderImpl.key(mac)
        verify(preferences).getString(key, "")
    }

    @Test
    fun get_noDate_returns_LastSyncData_withNullDate() {
        val mac = "mA:c2"
        val key = LastSyncProviderImpl.key(mac)
        whenever(preferences.getString(key, "")).thenReturn("")

        val result = lastSyncProvider.get(mac)

        Assert.assertEquals(result, NeverSync(mac))
    }

    @Test
    fun get_withDate_returns_LastSyncData_withDate() {
        val mac = "mA:c3"
        val key = LastSyncProviderImpl.key(mac)
        val textualDate = "01-01-2000"
        val date = TrustedClock.getNowZonedDateTime()
        whenever(preferences.getString(key, "")).thenReturn(textualDate)
        whenever(formatter.parse(textualDate)).thenReturn(date)

        val result = lastSyncProvider.get(mac)

        Assert.assertEquals(result, LastSyncDate(mac, date))
    }

    @Test
    fun put_invokesFormatter_format() {
        val mac = "mA:c4"
        val date = TrustedClock.getNowZonedDateTime()
        val textualDate = "02-02-2002"
        whenever(formatter.format(date)).thenReturn(textualDate)
        whenever(editor.putString(any(), any())).thenReturn(editor)

        lastSyncProvider.put(mac, date)
        verify(formatter).format(date)
    }

    @Test
    fun put_invokesEditor_putString() {
        val mac = "mA:c5"
        val date = TrustedClock.getNowZonedDateTime()
        val textualDate = "02-02-2002"
        whenever(formatter.format(date)).thenReturn(textualDate)
        whenever(editor.putString(any(), any())).thenReturn(editor)

        lastSyncProvider.put(mac, date)
        val key = LastSyncProviderImpl.key(mac)
        verify(editor).putString(key, textualDate)
    }
}

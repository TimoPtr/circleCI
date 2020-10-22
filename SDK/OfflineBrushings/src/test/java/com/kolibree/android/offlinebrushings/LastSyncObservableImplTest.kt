package com.kolibree.android.offlinebrushings

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.offlinebrushings.sync.LastSyncDate
import com.kolibree.android.offlinebrushings.sync.LastSyncObservableImpl
import com.kolibree.android.offlinebrushings.sync.LastSyncProvider
import com.kolibree.android.offlinebrushings.sync.NeverSync
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito

class LastSyncObservableImplTest : BaseUnitTest() {

    @Mock
    private lateinit var lastSyncProvider: LastSyncProvider

    private lateinit var lastSync: LastSyncObservableImpl

    override fun setup() {
        super.setup()

        lastSync = Mockito.spy(LastSyncObservableImpl(lastSyncProvider))
    }

    @Test
    fun getLastSyncData_invokes_refreshLastSyncDate() {
        val mac = "Tb:Ma:C1"
        whenever(lastSyncProvider.get(mac)).thenReturn(LastSyncDate(mac, TrustedClock.getNowZonedDateTime()))
        lastSync.getLastSyncData(mac)
        Mockito.verify(lastSyncProvider).get(mac)
    }

    @Test
    fun getLastSyncData_noDate_emitsNeverSync() {
        val mac = "Tb:Ma:C2"
        whenever(lastSyncProvider.get(mac)).thenReturn(NeverSync(mac))
        Assert.assertEquals(NeverSync(mac), lastSync.getLastSyncData(mac))
    }

    @Test
    fun getLastSyncData_withDate_emitsLastSyncDate() {
        val mac = "Tb:Ma:C3"
        val date = TrustedClock.getNowZonedDateTime()
        whenever(lastSyncProvider.get(mac)).thenReturn(LastSyncDate(mac, date))
        Assert.assertEquals(LastSyncDate(mac, date), lastSync.getLastSyncData(mac))
    }

    @Test
    fun observable_send_invokesPutMethod() {
        val mac = "Tb:Ma:C4"
        val date = TrustedClock.getNowZonedDateTime()
        doNothing().whenever(lastSyncProvider).put(mac, date)
        lastSync.send(LastSyncDate(mac, date))
        verify(lastSyncProvider).put(mac, date)
    }

    fun getLastSyncData_invokesGetMethod() {
        val mac = "Tb:Ma:C5"

        lastSync.getLastSyncData(mac)

        verify(lastSyncProvider).get(mac)
    }
}

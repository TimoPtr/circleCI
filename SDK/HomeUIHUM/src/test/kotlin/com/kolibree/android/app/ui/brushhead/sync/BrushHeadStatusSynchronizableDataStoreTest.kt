/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.brushhead.sync

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.brushhead.repo.BrushHeadRepository
import com.kolibree.android.app.ui.brushhead.sync.model.BrushHeadInformationSet
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.synchronizator.models.SynchronizableReadOnly
import com.kolibree.android.test.TestForcedException
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import junit.framework.TestCase.assertTrue
import org.junit.Test

class BrushHeadStatusSynchronizableDataStoreTest : BaseUnitTest() {
    private val dao: BrushHeadRepository = mock()
    private val versions: BrushHeadStatusSynchronizedVersions = mock()

    private val dataStore = BrushHeadStatusSynchronizableDataStore(dao, versions)

    @Test
    fun `updateVersion invokes setBrushHeadUsageVersion with expected value`() {
        val expectedVersion = 65
        dataStore.updateVersion(expectedVersion)

        verify(versions).setBrushHeadUsageVersion(expectedVersion)
    }

    /*
    replace
     */

    @Test
    fun `replace does nothing if parameter is not BrushHeadReplacedDates`() {
        val random = object : SynchronizableReadOnly {}

        dataStore.replace(random)
    }

    @Test
    fun `replace does nothing if BrushHeadReplacedDates is empty`() {
        val replacedDates = BrushHeadInformationSet(setOf())

        dataStore.replace(replacedDates)

        verifyNoMoreInteractions(dao)
    }

    @Test
    fun `replace replaces all entries in BrushHeadReplacedDates`() {
        val mac1 = "11"
        val date1 = TrustedClock.getNowOffsetDateTime().minusDays(1)

        val mac2 = "22"
        val date2 = TrustedClock.getNowOffsetDateTime().minusDays(2)
        val replacedDates = BrushHeadInformationSet(
            setOf(
                brushHeadInfo(mac = mac1, resetDate = date1),
                brushHeadInfo(mac = mac2, resetDate = date2)
            )
        )

        var writeDate1Subscribed = false
        val writeDate1Completable = Completable.complete()
            .doOnSubscribe { writeDate1Subscribed = true }
        whenever(dao.writeBrushHeadInfo(brushHeadInfo(mac1, date1)))
            .thenReturn(writeDate1Completable)

        var writeDate2Subscribed = false
        val writeDate2Completable = Completable.complete()
            .doOnSubscribe { writeDate2Subscribed = true }
        whenever(dao.writeBrushHeadInfo(brushHeadInfo(mac2, date2)))
            .thenReturn(writeDate2Completable)

        dataStore.replace(replacedDates)

        assertTrue(writeDate1Subscribed)
        assertTrue(writeDate2Subscribed)
    }

    @Test(expected = RuntimeException::class)
    fun `replace replaces all entries in BrushHeadReplacedDates, even if first write throws an error`() {
        val mac1 = "11"
        val date1 = TrustedClock.getNowOffsetDateTime().minusDays(1)

        val mac2 = "22"
        val date2 = TrustedClock.getNowOffsetDateTime().minusDays(2)
        val replacedDates = BrushHeadInformationSet(
            setOf(
                brushHeadInfo(mac = mac1, resetDate = date1),
                brushHeadInfo(mac = mac2, resetDate = date2)
            )
        )

        var writeDate1Subscribed = false
        val writeDate1Completable = Completable.error(TestForcedException())
            .doOnSubscribe { writeDate1Subscribed = true }
        whenever(dao.writeBrushHeadInfo(brushHeadInfo(mac1, date1)))
            .thenReturn(writeDate1Completable)

        var writeDate2Subscribed = false
        val writeDate2Subject = Completable.complete()
            .doOnSubscribe { writeDate2Subscribed = true }
        whenever(dao.writeBrushHeadInfo(brushHeadInfo(mac2, date2)))
            .thenReturn(writeDate2Subject)

        dataStore.replace(replacedDates)

        assertTrue(writeDate1Subscribed)
        assertTrue(writeDate2Subscribed)
    }
}

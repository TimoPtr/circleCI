/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.brushhead.repo

import android.content.Context
import android.content.SharedPreferences
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.brushhead.sync.brushHeadInfo
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.extensions.toEpochMilli
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset

internal class BrushHeadReplaceDateManagerTest : BaseUnitTest() {

    private lateinit var manager: BrushHeadReplaceDateManager

    private val context: Context = mock()

    private val preferences: SharedPreferences = mock()

    private val editor: SharedPreferences.Editor = mock()

    override fun setup() {
        super.setup()

        whenever(preferences.edit()).thenReturn(editor)
        whenever(editor.putLong(any(), any())).thenReturn(editor)
        whenever(context.applicationContext).thenReturn(context)
        whenever(context.getSharedPreferences(any(), anyInt())).thenReturn(preferences)

        manager = BrushHeadReplaceDateManager(context)
    }

    @Test
    fun `when replaced date is not present then manager, maybe completes without emitting a value`() {
        manager.read("mac:without:date").test().assertNoValues().assertComplete()
    }

    @Test
    fun `when replaced date is present then manager returns BrushHeadInformation`() {
        val mac = "mac:with:date"
        val expectedDateKey = BrushHeadReplaceDateManager.createDateReplaceKey(mac)
        val expectedReplacedDate = TrustedClock.getNowOffsetDateTime()
        whenever(preferences.getLong(expectedDateKey, -1))
            .thenReturn(expectedReplacedDate.toEpochMilli())

        val expectedPercentage = 67
        val expectedPercentageKey = BrushHeadReplaceDateManager.createPercentageKey(mac)
        whenever(preferences.getInt(expectedPercentageKey, 100))
            .thenReturn(expectedPercentage)

        val expectedInfo =
            brushHeadInfo(
                mac = mac,
                resetDate = expectedReplacedDate,
                percentageLeft = expectedPercentage
            )
        manager.read(mac).test().assertValue(expectedInfo)
    }

    @Test
    fun `writeReplacedDateNow stores BrushHeadInformation with current UTC timestamp and percentage=100`() {
        val expectedTimestamp = 10012345L
        TrustedClock.utcClock = Clock.fixed(Instant.ofEpochMilli(expectedTimestamp), ZoneOffset.UTC)
        TrustedClock.systemZone = ZoneOffset.UTC
        val expectedOffsetDate = OffsetDateTime.now(TrustedClock.utcClock)
        val mac = "01:22:FX"
        val expectedKey = BrushHeadReplaceDateManager.createDateReplaceKey(mac)

        val expectedInfo =
            brushHeadInfo(
                mac = mac,
                resetDate = expectedOffsetDate,
                percentageLeft = 100
            )

        manager.writeReplacedDateNow(mac).test().assertValue(expectedInfo)

        verify(editor).putLong(expectedKey, expectedTimestamp)
        verify(editor).apply()
    }

    @Test
    fun `writeReplacedDate stores the provided brushHeadInfo`() {
        val expectedOffsetDate = TrustedClock.getNowOffsetDateTime()
        val mac = "01:22:FX"
        val expectedDateKey = BrushHeadReplaceDateManager.createDateReplaceKey(mac)
        val expectedPercentageKey = BrushHeadReplaceDateManager.createPercentageKey(mac)

        val expectedPercentage = 53
        val brushHeadInfo = brushHeadInfo(mac, expectedOffsetDate, expectedPercentage)
        manager.writeBrushHeadInformation(brushHeadInfo).test().assertComplete()

        verify(editor).putLong(expectedDateKey, expectedOffsetDate.toEpochMilli())
        verify(editor).putInt(expectedPercentageKey, expectedPercentage)
        verify(editor).apply()
    }

    @Test
    fun `when user logs out all replaced dates are cleared`() {
        whenever(editor.clear()).thenReturn(editor)

        manager.truncate().test().assertComplete()

        verify(editor).clear()
    }
}

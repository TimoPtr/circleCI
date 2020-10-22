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
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.clock.TrustedClock.systemZoneOffset
import com.kolibree.android.room.ZoneOffsetConverter
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.temporal.ChronoUnit

class BrushHeadDateSendApiProviderImplTest : BaseUnitTest() {

    private val preferences: SharedPreferences = mock()

    private lateinit var brushHeadDateSendApiProvider: BrushHeadDateSendApiProviderImpl

    override fun setup() {
        super.setup()
        val context: Context = mock()
        whenever(context.applicationContext).thenReturn(context)
        whenever(context.getSharedPreferences(any(), any())).thenReturn(preferences)

        brushHeadDateSendApiProvider = BrushHeadDateSendApiProviderImpl(context)
    }

    @Test
    fun `getLastReplacedDateSent send the right date if timestamp and zoneOffset has been set`() {

        val (expectedDateOffset, mac) = getLastReplacedDateSentHappyPath()

        brushHeadDateSendApiProvider.getLastReplacedDateSentMaybe(mac)
            .test()
            .assertValue(expectedDateOffset)
            .assertComplete()
    }

    @Test
    fun `getLastReplacedDateSent should be empty if timestamp has not been set`() {
        val (_, mac) = getLastReplacedDateSentHappyPath()

        whenever(preferences.getLong("$mac$LAST_DATE_SENT_TIMESTAMP", DEFAULT_TIMESTAMP))
            .thenReturn(DEFAULT_TIMESTAMP)

        brushHeadDateSendApiProvider.getLastReplacedDateSentMaybe(mac)
            .test()
            .assertNoValues()
            .assertComplete()
    }

    @Test
    fun `getLastReplacedDateSent should be empty if zoneOffset has not been set`() {
        val (_, mac) = getLastReplacedDateSentHappyPath()

        whenever(preferences.getString("$mac$LAST_DATE_SENT_OFFSET", null))
            .thenReturn(null)

        brushHeadDateSendApiProvider.getLastReplacedDateSentMaybe(mac)
            .test()
            .assertNoValues()
            .assertComplete()
    }

    @Test
    fun `setLastReplacedDateSentCompletable should set the date timestamp in seconds`() {
        val mac = "mac-123"
        val expectedDate = TrustedClock.getNowOffsetDateTime()
        val editor: SharedPreferences.Editor = mock()

        whenever(preferences.edit()).thenReturn(editor)

        brushHeadDateSendApiProvider.setLastReplacedDateSentCompletable(mac, expectedDate)
            .test()
            .assertComplete()

        inOrder(editor) {
            this.verify(editor)
                .putLong("$mac$LAST_DATE_SENT_TIMESTAMP", expectedDate.toEpochSecond())
            this.verify(editor).putString(
                "$mac$LAST_DATE_SENT_OFFSET",
                ZoneOffsetConverter.fromZoneOffset(systemZoneOffset)
            )
            this.verify(editor).apply()
        }
    }

    private fun getLastReplacedDateSentHappyPath(): Pair<OffsetDateTime, String> {
        val expectedDateOffset = TrustedClock.getNowOffsetDateTime().truncatedTo(ChronoUnit.SECONDS)

        val mac = "123"

        whenever(preferences.getLong("$mac$LAST_DATE_SENT_TIMESTAMP", DEFAULT_TIMESTAMP))
            .thenReturn(expectedDateOffset.toEpochSecond())
        whenever(preferences.getString("$mac$LAST_DATE_SENT_OFFSET", null))
            .thenReturn(ZONE_ID)

        return expectedDateOffset to mac
    }
}

private val ZONE_ID = systemZoneOffset.id
private const val DEFAULT_TIMESTAMP = -1L
private const val LAST_DATE_SENT_TIMESTAMP = "_last_date_sent_timestamp"
private const val LAST_DATE_SENT_OFFSET = "_last_date_sent_offset"

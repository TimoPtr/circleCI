/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.sync.job

import com.kolibree.android.KOLIBREE_DAY_START_HOUR
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test

internal class OfflineBrushingsNotificationContentExtensionsTest : BaseUnitTest() {
    /*
    dateToShow
     */
    @Test
    fun `dateToShow returns null if OfflineBrushingNotificationContent contains zero brushings`() {
        val notificationContent = createOfflineBrushingNotificationContent()

        assertNull(notificationContent.dateToShow())
    }

    @Test
    fun `dateToShow returns null if OfflineBrushingNotificationContent contains orphan brushings`() {
        val notificationContent =
            createOfflineBrushingNotificationContent(orphanBrushingsDateTimes = listOf(TrustedClock.getNowOffsetDateTime()))

        assertNull(notificationContent.dateToShow())
    }

    @Test
    fun `dateToShow returns epoch milli from most recent brushing if OfflineBrushingNotificationContent multiple offline brushing dates and zero orphan brushings`() {
        val noonToday = TrustedClock.getNowOffsetDateTime().withHour(12)
        val noonYesterday = noonToday.minusDays(1)
        val notificationContent =
            createOfflineBrushingNotificationContent(
                offlineBrushingsDateTimes = listOf(noonToday, noonYesterday)
            )

        assertEquals(noonToday.toInstant().toEpochMilli(), notificationContent.dateToShow())
    }

    @Test
    fun `dateToShow returns epoch milli from most recent brushing if OfflineBrushingNotificationContent contains multiple KolibreeDay`() {
        val startKolibreeDay = TrustedClock.getNowOffsetDateTime().withHour(KOLIBREE_DAY_START_HOUR)
        val previousKolibreeDay = startKolibreeDay.minusHours(1)

        // LocalDate is equal, but kolibree day is different
        assertEquals(startKolibreeDay.toLocalDate(), previousKolibreeDay.toLocalDate())

        val notificationContent =
            createOfflineBrushingNotificationContent(
                offlineBrushingsDateTimes = listOf(startKolibreeDay, previousKolibreeDay)
            )

        assertEquals(startKolibreeDay.toInstant().toEpochMilli(), notificationContent.dateToShow())
    }

    @Test
    fun `dateToShow returns brushing epoch milli at UTC if OfflineBrushingNotificationContent contains single LocalDateTime`() {
        val noon = TrustedClock.getNowOffsetDateTime().withHour(12)
        val notificationContent =
            createOfflineBrushingNotificationContent(offlineBrushingsDateTimes = listOf(noon))

        assertEquals(noon.toInstant().toEpochMilli(), notificationContent.dateToShow())
    }

    @Test
    fun `dateToShow returns epoch milli from most recent brushing if OfflineBrushingNotificationContent contains multiple offline brushings on the same kolibree day`() {
        val noon = TrustedClock.getNowOffsetDateTime().withHour(12)
        val eleven = noon.minusHours(1)
        val onePM = noon.plusHours(1)
        val notificationContent =
            createOfflineBrushingNotificationContent(
                offlineBrushingsDateTimes = listOf(
                    noon,
                    onePM,
                    eleven
                )
            )

        assertEquals(onePM.toInstant().toEpochMilli(), notificationContent.dateToShow())
    }

    /*
    notificationCode
     */
    @Test
    fun `notificationCode returns OFFLINE_BRUSHING_EXTRACTED if there's 0 orphan and 0 Offline`() {
        val notificationContent =
            createOfflineBrushingNotificationContentFromNumber(
                totalOrphanBrushings = 0,
                totalOfflineBrushings = 0
            )

        assertEquals(OFFLINE_BRUSHING_EXTRACTED, notificationContent.notificationCode())
    }

    @Test
    fun `notificationCode returns OFFLINE_BRUSHING_EXTRACTED if there's 0 orphan and 1 Offline`() {
        val notificationContent =
            createOfflineBrushingNotificationContentFromNumber(
                totalOrphanBrushings = 0,
                totalOfflineBrushings = 1
            )

        assertEquals(OFFLINE_BRUSHING_EXTRACTED, notificationContent.notificationCode())
    }

    @Test
    fun `notificationCode returns OFFLINE_BRUSHING_EXTRACTED if there's 0 orphan and more than 1 Offline on same kolibree day`() {
        val noon = TrustedClock.getNowOffsetDateTime().withHour(12)
        val notificationContent =
            createOfflineBrushingNotificationContent(
                offlineBrushingsDateTimes = listOf(
                    noon,
                    noon.plusHours(1),
                    noon.minusHours(1)
                )
            )

        assertEquals(
            OFFLINE_BRUSHING_EXTRACTED,
            notificationContent.notificationCode()
        )
    }

    @Test
    fun `notificationCode returns OFFLINE_BRUSHING_EXTRACTED if there's 0 orphan and more than 1 Offline on different day`() {
        val notificationContent =
            createOfflineBrushingNotificationContent(
                offlineBrushingsDateTimes = listOf(
                    TrustedClock.getNowOffsetDateTime(),
                    TrustedClock.getNowOffsetDateTime().minusDays(1)
                )
            )

        assertEquals(
            OFFLINE_BRUSHING_EXTRACTED,
            notificationContent.notificationCode()
        )
    }

    @Test
    fun `notificationCode returns OFFLINE_BRUSHING_EXTRACTED if there's 0 orphan and more than 1 Offline on different kolibree day`() {
        val startKolibreeDay = TrustedClock.getNowOffsetDateTime().withHour(KOLIBREE_DAY_START_HOUR)
        val previousKolibreeDay = startKolibreeDay.minusHours(1)

        // LocalDate is equal, but kolibree day is different
        assertEquals(startKolibreeDay.toLocalDate(), previousKolibreeDay.toLocalDate())

        val notificationContent =
            createOfflineBrushingNotificationContent(
                offlineBrushingsDateTimes = listOf(startKolibreeDay, previousKolibreeDay)
            )

        assertEquals(
            OFFLINE_BRUSHING_EXTRACTED,
            notificationContent.notificationCode()
        )
    }

    @Test
    fun `notificationCode returns ORPHAN_BRUSHING_EXTRACTED if there's at least 1 orphan and zero Offline`() {
        val notificationContent =
            createOfflineBrushingNotificationContentFromNumber(
                totalOrphanBrushings = 1,
                totalOfflineBrushings = 0
            )

        assertEquals(ORPHAN_BRUSHING_EXTRACTED, notificationContent.notificationCode())
    }

    @Test
    fun `notificationCode returns OFFLINE_BRUSHING_EXTRACTED if there's at least 1 orphan and 1 Offline`() {
        val notificationContent =
            createOfflineBrushingNotificationContentFromNumber(
                totalOrphanBrushings = 1,
                totalOfflineBrushings = 1
            )

        assertEquals(OFFLINE_BRUSHING_EXTRACTED, notificationContent.notificationCode())
    }

    @Test
    fun `notificationCode returns OFFLINE_BRUSHING_EXTRACTED if there's at least 1 orphan and more than 1 Offline on same kolibree day`() {
        val noon = TrustedClock.getNowOffsetDateTime().withHour(12)
        val notificationContent =
            createOfflineBrushingNotificationContent(
                offlineBrushingsDateTimes = listOf(
                    noon,
                    noon.plusHours(1),
                    noon.minusHours(1)
                )
            )

        assertEquals(OFFLINE_BRUSHING_EXTRACTED, notificationContent.notificationCode())
    }

    @Test
    fun `notificationCode returns OFFLINE_BRUSHING_EXTRACTED if there's at least 1 orphan and more than 1 Offline on different day`() {
        val notificationContent =
            createOfflineBrushingNotificationContent(
                offlineBrushingsDateTimes = listOf(
                    TrustedClock.getNowOffsetDateTime(),
                    TrustedClock.getNowOffsetDateTime().minusDays(1)
                )
            )

        assertEquals(OFFLINE_BRUSHING_EXTRACTED, notificationContent.notificationCode())
    }

    @Test
    fun `notificationCode returns OFFLINE_BRUSHING_EXTRACTED if there's at least 1 orphan and more than 1 Offline on different kolibree day`() {
        val startKolibreeDay = TrustedClock.getNowOffsetDateTime().withHour(KOLIBREE_DAY_START_HOUR)
        val previousKolibreeDay = startKolibreeDay.minusHours(1)

        // LocalDate is equal, but kolibree day is different
        assertEquals(startKolibreeDay.toLocalDate(), previousKolibreeDay.toLocalDate())

        val notificationContent =
            createOfflineBrushingNotificationContent(
                offlineBrushingsDateTimes = listOf(startKolibreeDay, previousKolibreeDay)
            )

        assertEquals(OFFLINE_BRUSHING_EXTRACTED, notificationContent.notificationCode())
    }
}

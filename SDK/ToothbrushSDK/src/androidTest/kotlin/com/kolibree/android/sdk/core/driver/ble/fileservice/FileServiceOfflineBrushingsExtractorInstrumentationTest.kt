/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.driver.ble.fileservice

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.OfflineBrushing
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileServiceInteractor
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileServiceOfflineBrushingsExtractor
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileSession
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.legacy.toothbrushTimestampToOffsetDateTime
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.kml.Kml
import com.kolibree.kml.ProcessedBrushing
import com.kolibree.kml.ProcessedBrushing16
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.threeten.bp.Duration
import org.threeten.bp.temporal.ChronoUnit

internal class FileServiceOfflineBrushingsExtractorInstrumentationTest : BaseInstrumentationTest() {
    init {
        Kml.init()
    }

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val fileServiceInteractor: FileServiceInteractor = mock()

    private val offlineExtractor = FileServiceOfflineBrushingsExtractor(fileServiceInteractor)

    @Test
    fun getNextBrushingRecord_maps_KLTBFile_to_ProcessedBrushing16() {
        val session: FileSession = mock()

        val processedBrushing16 = mock<ProcessedBrushing16>()
        val epochTimestamp = TrustedClock.systemClock().instant().epochSecond
        whenever(processedBrushing16.timestampInSeconds).thenReturn(epochTimestamp)

        val expectedDuration = TimeUnit.SECONDS.toMillis(120)
        whenever(processedBrushing16.durationInMilliseconds).thenReturn(expectedDuration)

        val expectedJson = "dada"
        val processedBrushing = mock<ProcessedBrushing>()
        whenever(processedBrushing.toJSON()).thenReturn(expectedJson)
        whenever(processedBrushing16.toProcessedBrushing()).thenReturn(processedBrushing)

        whenever(session.getSelectedFile()).thenReturn(Single.just(processedBrushing16))

        val expectedDatetime = epochTimestamp.toothbrushTimestampToOffsetDateTime().toLocalDateTime()

        val expectedOfflineBrushing = object : OfflineBrushing {
            override val datetime = expectedDatetime
            override val duration = Duration.of(expectedDuration, ChronoUnit.MILLIS)
            override val processedData: String = expectedJson
        }

        val actualOfflineBrushing =
            offlineExtractor.getNextBrushingRecord(session).test().values().single()

        assertEquals(expectedOfflineBrushing.duration, actualOfflineBrushing.duration)
        assertEquals(expectedOfflineBrushing.processedData, actualOfflineBrushing.processedData)
        assertEquals(expectedOfflineBrushing.datetime, actualOfflineBrushing.datetime)
    }
}

/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.secret.fakebrushings

import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.processedbrushings.computeProcessedData
import com.kolibree.kml.MouthZone16
import org.json.JSONObject
import org.threeten.bp.Clock
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime

class FakeProcessedDataBuilder {

    /**
     * Builds a V1 Processed data
     *
     * It distributes [realDuration] uniformly across each [MouthZone16]
     */
    fun build(
        dateTime: LocalDateTime,
        goalDuration: Duration,
        realDuration: Duration = goalDuration
    ): String {
        val instant = dateTime.toInstant(TrustedClock.systemZoneOffset)
        val processedDataBuilder = ProcessedDataBuilder(instant.toFixedClock())

        processedDataBuilder.start(true)

        processedDataBuilder.distributeUniformPasses(realDuration, instant)

        val zoneMap = processedDataBuilder.stop()
        val processedData: String = computeProcessedData(zoneMap, goalDuration.seconds.toInt())

        val jsonObject = JSONObject(processedData)
        jsonObject.put("version", 1)

        return jsonObject.toString()
    }

    private fun ProcessedDataBuilder.distributeUniformPasses(realDuration: Duration, instant: Instant) {
        var innerInstant = instant
        val secondsPerZone = realDuration.seconds / MouthZone16.values().size
        MouthZone16.values().forEach { zone ->
            onMouthZoneDetection(zone)
            innerInstant = innerInstant.plusSeconds(secondsPerZone)
            setClock(innerInstant.toFixedClock())
        }

        onVibratorStateChanged(false)
    }

    private fun Instant.toFixedClock() = Clock.fixed(this, TrustedClock.systemZone)
}

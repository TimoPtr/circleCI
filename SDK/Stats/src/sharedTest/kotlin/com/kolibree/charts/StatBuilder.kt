/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.charts

import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.extensions.toEpochMilli
import com.kolibree.charts.models.Stat
import java.util.Random
import org.threeten.bp.OffsetDateTime

/**
 * Created by guillaumeagis on 22/05/18.
 * Create stat object
 */

internal class StatBuilder private constructor() {

    private var duration: Long = 0
    private var brushedSurface: Int = 0
    private val clock = TrustedClock.utcClock
    private var timestamp = OffsetDateTime.now(clock).toInstant().toEpochMilli()
    private var processedData = ""

    fun withDuration(duration: Long): StatBuilder {
        this.duration = duration

        return this
    }

    fun withDateTime(dateTime: OffsetDateTime): StatBuilder {
        timestamp = dateTime.toEpochMilli()

        return this
    }

    fun withProcessedData(processedData: String): StatBuilder {
        this.processedData = processedData

        return this
    }

    fun withAverageBrushedSurface(brushedSurface: Int): StatBuilder {
        this.brushedSurface = brushedSurface

        return this
    }

    fun build(): Stat {
        return Stat(Random().nextLong(), duration, timestamp, clock, brushedSurface, processedData)
    }

    companion object {

        fun create(): StatBuilder {
            return StatBuilder()
        }
    }
}

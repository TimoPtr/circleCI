package com.kolibree.sdkws.brushing

import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit

// extend the zonedDateTime object
internal fun ZonedDateTime.getTimestampSecondsPrecision(): Long {
    return this.toInstant().truncatedTo(ChronoUnit.SECONDS).toEpochMilli()
}

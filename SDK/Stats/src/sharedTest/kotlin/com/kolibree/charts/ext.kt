package com.kolibree.charts

import org.threeten.bp.ZonedDateTime

internal fun ZonedDateTime.getTimestamp() = this.toInstant().toEpochMilli()

/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.test

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.YearMonth
import org.threeten.bp.ZonedDateTime

internal fun LocalDateTime.toYearMonth() = YearMonth.from(this)
internal fun LocalDate.toYearMonth() = YearMonth.from(this)
internal fun ZonedDateTime.toYearMonth() = YearMonth.from(this)
internal fun OffsetDateTime.toYearMonth() = YearMonth.from(this)

internal fun randomListOfDouble(size: Int): List<Double> =
    (0 until size).fold(mutableListOf(), { list, _ ->
        list.add(randomPercentageDouble())

        list
    })

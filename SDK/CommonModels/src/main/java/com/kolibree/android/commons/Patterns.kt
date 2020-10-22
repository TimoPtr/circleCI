/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

@file:JvmName("ApiConstants")

package com.kolibree.android.commons

import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeFormatter.ofPattern

const val WEEK_DATE_PATTERN = "EEE, MMM dd"

const val DATE_PATTERN = "yyyy-MM-dd"

const val DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ"

const val ZONE_PATTERN = "Z"

const val DATETIME_PATTERN_NO_ZONE = "yyyy-MM-dd'T'HH:mm:ss"

const val MONTH_YEAR_PATTERN = "MMMM yyyy"

const val SHORT_MONTH_FORMAT = "MMM"

const val SHORT_MONTH_YEAR_PATTERN = "MMM yyyy"

const val HOUR_12H_PATTERN = "hh:mm a"

const val HOUR_24H_PATTERN = "HH:mm"

@JvmField
val DATE_FORMATTER: DateTimeFormatter = ofPattern(DATE_PATTERN)

@JvmField
val WEEK_DATE_FORMATTER: DateTimeFormatter = ofPattern(WEEK_DATE_PATTERN)

@JvmField
val DATETIME_FORMATTER: DateTimeFormatter = ofPattern(DATETIME_PATTERN)

@JvmField
val ZONE_FORMATTER: DateTimeFormatter = ofPattern(ZONE_PATTERN)

@JvmField
val DATETIME_FORMATTER_NO_ZONE: DateTimeFormatter = ofPattern(DATETIME_PATTERN_NO_ZONE)

@JvmField
val MONTH_YEAR_FORMATTER: DateTimeFormatter = ofPattern(MONTH_YEAR_PATTERN)

@JvmField
val SHORT_MONTH_FORMATTER: DateTimeFormatter = ofPattern(SHORT_MONTH_FORMAT)

@JvmField
val SHORT_MONTH_YEAR_FORMATTER: DateTimeFormatter = ofPattern(SHORT_MONTH_YEAR_PATTERN)

@JvmField
val HOUR_12H_FORMATTER: DateTimeFormatter = ofPattern(HOUR_12H_PATTERN)

@JvmField
val HOUR_24H_FORMATTER: DateTimeFormatter = ofPattern(HOUR_24H_PATTERN)

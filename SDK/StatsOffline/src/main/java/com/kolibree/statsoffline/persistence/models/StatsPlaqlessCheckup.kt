/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.persistence.models

import android.annotation.SuppressLint
import android.os.Parcelable
import com.kolibree.kml.MouthZone16
import com.kolibree.kml.PlaqueStatus
import kotlinx.android.parcel.Parcelize

/**
 * Aggregated stats Lite version of KML's PlaqlessCheckup
 *
 * By not usuing KML class, we have the flexibility to decouple KML implementation from the stored
 * aggregated stats
 */
@SuppressLint("DeobfuscatedPublicSdkClass")
@Parcelize
data class StatsPlaqlessCheckup(
    val cleanPercent: Int?,
    val missedPercent: Int?,
    val plaqueLeftPercent: Int?,
    val plaqueAggregate: Map<MouthZone16, StatsPlaqueAggregate>
) : Parcelable

@SuppressLint("DeobfuscatedPublicSdkClass")
@Parcelize
data class StatsPlaqueAggregate(val status: PlaqueStatus, val cleannessPercent: Int) : Parcelable

/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.toothbrush.battery

import androidx.annotation.Keep

/** Discrete battery level definition */
/* https://confluence.kolibree.com/display/PROD/Toothbrush+battery+management */
@Keep
enum class DiscreteBatteryLevel {

    /*
    More than 6 months of battery lifetime available assuming you use our standard AAA alkaline
    battery from Energizer. Other types of batteries may lead to lower battery life.
     */
    BATTERY_6_MONTHS,

    /*
    More than 3 months of battery lifetime available assuming you use our standard AAA alkaline
    battery from Energizer. Other types of batteries may lead to lower battery life.
     */
    BATTERY_3_MONTHS,

    /*
    Still few weeks of battery lifetime available assuming you use our standard AAA alkaline battery
    from Energizer. Other types of batteries may lead to lower battery life.
     */
    BATTERY_FEW_WEEKS,

    /*
    Only few days of battery lifetime available, get ready to replace it.
     */
    BATTERY_FEW_DAYS,

    /*
    Change your battery immediately.
     */
    BATTERY_CHANGE,

    /*
    Internal, please report if you get this value
     */
    BATTERY_CUT_OFF,

    /*
    Unknown battery state
     */
    BATTERY_UNKNOWN
}

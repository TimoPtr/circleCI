/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.pirate.controller

import com.kolibree.kml.MouthZone8

internal object World1Constant {

    enum class KLPirateLevel1PrescribedZone {
        KLPirateLevel1PrescribedZoneNone,
        KLPirateLevel1PrescribedZoneLeftExtBottom,
        KLPirateLevel1PrescribedZoneRightExtBottom,
        KLPirateLevel1PrescribedZoneLeftExtTop,
        KLPirateLevel1PrescribedZoneRightExtTop,
        KLPirateLevel1PrescribedZoneLeftIntBottom,
        KLPirateLevel1PrescribedZoneRightIntBottom,
        KLPirateLevel1PrescribedZoneLeftIntTop,
        KLPirateLevel1PrescribedZoneRightIntTop;

        fun toMouthZone(): MouthZone8? = when (this) {
            KLPirateLevel1PrescribedZoneLeftExtBottom -> MouthZone8.LoLeExt
            KLPirateLevel1PrescribedZoneRightExtBottom -> MouthZone8.LoRiExt
            KLPirateLevel1PrescribedZoneLeftExtTop -> MouthZone8.UpLeExt
            KLPirateLevel1PrescribedZoneRightExtTop -> MouthZone8.UpRiExt
            KLPirateLevel1PrescribedZoneLeftIntBottom -> MouthZone8.LoLeInt
            KLPirateLevel1PrescribedZoneRightIntBottom -> MouthZone8.LoRiInt
            KLPirateLevel1PrescribedZoneLeftIntTop -> MouthZone8.UpLeInt
            KLPirateLevel1PrescribedZoneRightIntTop -> MouthZone8.UpRiInt
            KLPirateLevel1PrescribedZoneNone -> null
        }
    }
}

/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.pirate.controller

import com.kolibree.kml.MouthZone16

internal object World3Constant {

    enum class KLPirateLevel3PrescribedZone {
        KLPirateLevel3PrescribedZoneNone,
        KLPirateLevel3PrescribedZoneLeftOccBottom,
        KLPirateLevel3PrescribedZoneRightExtBottom,
        KLPirateLevel3PrescribedZoneIncExtBottom,
        KLPirateLevel3PrescribedZoneRightOccBottom,
        KLPirateLevel3PrescribedZoneLeftExtBottom,
        KLPirateLevel3PrescribedZoneIncIntBottom,
        KLPirateLevel3PrescribedZoneRightIntBottom,
        KLPirateLevel3PrescribedZoneLeftIntBottom,
        KLPirateLevel3PrescribedZoneRightOccTop,
        KLPirateLevel3PrescribedZoneIncExtTop,
        KLPirateLevel3PrescribedZoneLeftOccTop,
        KLPirateLevel3PrescribedZoneRightExtTop,
        KLPirateLevel3PrescribedZoneLeftExtTop,
        KLPirateLevel3PrescribedZoneIncIntTop,
        KLPirateLevel3PrescribedZoneRightIntTop,
        KLPirateLevel3PrescribedZoneLeftIntTop;

        fun toMouthZone(): MouthZone16? = when (this) {
            KLPirateLevel3PrescribedZoneLeftOccBottom -> MouthZone16.LoMolLeOcc
            KLPirateLevel3PrescribedZoneRightExtBottom -> MouthZone16.LoMolRiExt
            KLPirateLevel3PrescribedZoneIncExtBottom -> MouthZone16.LoIncExt
            KLPirateLevel3PrescribedZoneRightOccBottom -> MouthZone16.LoMolRiOcc
            KLPirateLevel3PrescribedZoneLeftExtBottom -> MouthZone16.LoMolLeExt
            KLPirateLevel3PrescribedZoneIncIntBottom -> MouthZone16.LoIncInt
            KLPirateLevel3PrescribedZoneRightIntBottom -> MouthZone16.LoMolRiInt
            KLPirateLevel3PrescribedZoneLeftIntBottom -> MouthZone16.LoMolLeInt
            KLPirateLevel3PrescribedZoneRightOccTop -> MouthZone16.UpMolRiOcc
            KLPirateLevel3PrescribedZoneIncExtTop -> MouthZone16.UpIncExt
            KLPirateLevel3PrescribedZoneLeftOccTop -> MouthZone16.UpMolLeOcc
            KLPirateLevel3PrescribedZoneRightExtTop -> MouthZone16.UpMolRiExt
            KLPirateLevel3PrescribedZoneLeftExtTop -> MouthZone16.UpMolLeExt
            KLPirateLevel3PrescribedZoneIncIntTop -> MouthZone16.UpIncInt
            KLPirateLevel3PrescribedZoneRightIntTop -> MouthZone16.UpMolRiInt
            KLPirateLevel3PrescribedZoneLeftIntTop -> MouthZone16.UpMolLeInt
            KLPirateLevel3PrescribedZoneNone -> null
        }
    }
}

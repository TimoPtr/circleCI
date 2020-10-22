/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.pirate.controller

import com.kolibree.kml.MouthZone12

internal object World2Constant {

    enum class KLPirateLevel2PrescribedZone {
        KLPirateLevel2PrescribedZoneNone,
        KLPirateLevel2PrescribedZoneLeftExtBottom,
        KLPirateLevel2PrescribedZoneRightExtBottom,
        KLPirateLevel2PrescribedZoneIncExtBottom,
        KLPirateLevel2PrescribedZoneLeftIntBottom,
        KLPirateLevel2PrescribedZoneIncIntBottom,
        KLPirateLevel2PrescribedZoneRightIntBottom,
        KLPirateLevel2PrescribedZoneLeftExtTop,
        KLPirateLevel2PrescribedZoneRightExtTop,
        KLPirateLevel2PrescribedZoneIncExtTop,
        KLPirateLevel2PrescribedZoneLeftIntTop,
        KLPirateLevel2PrescribedZoneIncIntTop,
        KLPirateLevel2PrescribedZoneRightIntTop;

        fun toMouthZone(): MouthZone12? = when (this) {
            KLPirateLevel2PrescribedZoneLeftExtBottom -> MouthZone12.LoMolLeExt12
            KLPirateLevel2PrescribedZoneRightExtBottom -> MouthZone12.LoMolRiExt12
            KLPirateLevel2PrescribedZoneIncExtBottom -> MouthZone12.LoIncExt12
            KLPirateLevel2PrescribedZoneLeftIntBottom -> MouthZone12.LoMolLeInt12
            KLPirateLevel2PrescribedZoneIncIntBottom -> MouthZone12.LoIncInt12
            KLPirateLevel2PrescribedZoneRightIntBottom -> MouthZone12.LoMolRiInt12
            KLPirateLevel2PrescribedZoneLeftExtTop -> MouthZone12.UpMolLeExt12
            KLPirateLevel2PrescribedZoneRightExtTop -> MouthZone12.UpMolRiExt12
            KLPirateLevel2PrescribedZoneIncExtTop -> MouthZone12.UpIncExt12
            KLPirateLevel2PrescribedZoneLeftIntTop -> MouthZone12.UpMolLeInt12
            KLPirateLevel2PrescribedZoneIncIntTop -> MouthZone12.UpIncInt12
            KLPirateLevel2PrescribedZoneRightIntTop -> MouthZone12.UpMolRiInt12
            KLPirateLevel2PrescribedZoneNone -> null
        }
    }
}

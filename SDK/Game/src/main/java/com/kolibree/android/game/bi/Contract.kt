/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.bi

import com.kolibree.android.annotation.VisibleForApp

/**
 * AVRO contract
 *
 * https://docs.google.com/document/d/1NeIzYrm-T_aB_QHYSEwg29Ex0M5LvgVsEti2Jcvir9A
 *
 * Do not change this unless Yann Nicolas is aware of the changes
 */
@VisibleForApp
object Contract {

    @VisibleForApp
    object ActivityName {
        const val COACH = "C"
        const val COACH_PLUS = COACH
        const val GO_PIRATE = "P"
        const val RABBIDS = "R"
        const val FREE_BRUSHING = "F"
    }

    @VisibleForApp
    object BrushingMode {
        const val MANUAL = "M"
        const val VIBRATING = "V"
    }

    @VisibleForApp
    object Handedness {
        const val RIGHT_HANDED = "R"
        const val LEFT_HANDED = "L"
        const val UNKNOWN = "N"
    }

    @VisibleForApp
    object ToothbrushModelName {
        const val ARA = "ara"
        const val M1 = "cm1"
        const val E1 = "ce1"
        const val E2 = "ce2"
        const val B1 = "cb1"
        const val PQL = "pql"
        const val GLI = "gli"
    }
}

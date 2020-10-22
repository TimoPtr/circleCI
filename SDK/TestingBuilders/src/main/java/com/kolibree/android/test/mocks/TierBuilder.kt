/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.mocks

import android.os.Parcel
import androidx.annotation.Keep
import com.kolibree.android.rewards.models.Tier

@Keep
fun createTier(
    level: Int = 1,
    smilesPerBrushing: Int = 1,
    challengesNeeded: Int = 1,
    pictureUrl: String = "",
    rank: String = ""
): Tier {
    return object : Tier {
        override fun writeToParcel(dest: Parcel?, flags: Int) {
            TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
        }

        override fun describeContents(): Int {
            TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
        }

        override val level: Int = level
        override val smilesPerBrushing: Int = smilesPerBrushing
        override val challengesNeeded: Int = challengesNeeded
        override val pictureUrl: String = pictureUrl
        override val rank: String = rank
    }
}

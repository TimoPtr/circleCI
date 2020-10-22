/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.coach.animation

import androidx.annotation.VisibleForTesting
import com.kolibree.kml.MouthZone16

/** Base [BrushHeadAnimationMapper] implementation */
internal abstract class BaseBrushHeadAnimationMapper : BrushHeadAnimationMapper {

    abstract val upIncIntAnimation: Animation

    abstract val loIncIntAnimation: Animation

    override fun getAnimationForZone(zone16: MouthZone16) =
        when (zone16) {
            MouthZone16.LoMolLeOcc,
            MouthZone16.LoMolRiOcc,
            MouthZone16.UpMolLeOcc,
            MouthZone16.UpMolRiOcc -> simpleAnimation

            MouthZone16.UpIncInt -> upIncIntAnimation

            MouthZone16.LoIncInt -> loIncIntAnimation

            MouthZone16.LoMolRiExt,
            MouthZone16.UpMolLeExt,
            MouthZone16.UpIncExt,
            MouthZone16.UpMolRiInt,
            MouthZone16.LoMolLeInt,
            MouthZone16.LoIncExt,
            MouthZone16.LoMolLeExt,
            MouthZone16.LoMolRiInt,
            MouthZone16.UpMolRiExt,
            MouthZone16.UpMolLeInt -> brushHeadAnimation
        }

    companion object {

        @VisibleForTesting
        internal val brushHeadAnimation: Animation = BrushHeadAnimation()

        @VisibleForTesting
        internal val simpleAnimation: Animation = TranslationAnimation()
    }
}

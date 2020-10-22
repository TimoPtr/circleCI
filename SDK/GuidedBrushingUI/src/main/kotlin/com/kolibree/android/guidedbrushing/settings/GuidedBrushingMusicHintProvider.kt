/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.settings

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.coachplus.utils.MusicHintProvider
import com.kolibree.android.guidedbrushing.R

@VisibleForApp
object GuidedBrushingMusicHintProvider : MusicHintProvider {
    override fun provideNoArtistString(): Int = R.string.value_not_available

    override fun provideNoFileString(): Int = R.string.value_not_available

    override fun provideNoTitleString(): Int = R.string.value_not_available
}

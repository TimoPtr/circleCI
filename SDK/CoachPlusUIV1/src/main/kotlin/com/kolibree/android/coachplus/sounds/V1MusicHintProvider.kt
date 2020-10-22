/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.sounds

import androidx.annotation.Keep
import com.kolibree.android.coachplus.R
import com.kolibree.android.coachplus.utils.MusicHintProvider

@Keep
object V1MusicHintProvider : MusicHintProvider {

    override fun provideNoArtistString(): Int = R.string.no_artist

    override fun provideNoFileString(): Int = R.string.no_music_file

    override fun provideNoTitleString(): Int = R.string.no_title
}

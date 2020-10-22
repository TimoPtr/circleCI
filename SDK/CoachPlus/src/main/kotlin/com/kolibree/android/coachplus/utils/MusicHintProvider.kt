/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.utils

import androidx.annotation.Keep
import androidx.annotation.StringRes

@Keep
interface MusicHintProvider {

    @StringRes
    fun provideNoArtistString(): Int

    @StringRes
    fun provideNoFileString(): Int

    @StringRes
    fun provideNoTitleString(): Int
}

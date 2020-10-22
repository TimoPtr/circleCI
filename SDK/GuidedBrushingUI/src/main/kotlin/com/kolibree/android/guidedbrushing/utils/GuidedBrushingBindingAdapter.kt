/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.utils

import android.net.Uri
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.kolibree.android.coachplus.utils.MusicUtils
import com.kolibree.android.guidedbrushing.settings.GuidedBrushingMusicHintProvider

@BindingAdapter("musicUri")
internal fun TextView.setMusicFromUri(musicUri: Uri?) {
    text = MusicUtils.getCoachMusicFileInfo(context, GuidedBrushingMusicHintProvider, musicUri)
}

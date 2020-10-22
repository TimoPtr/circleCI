/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.settings

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kolibree.android.app.base.BaseNavigator
import javax.inject.Inject

internal class GuidedBrushingSettingsNavigator : BaseNavigator<GuidedBrushingSettingsActivity>() {

    private lateinit var openAudioDocumentContract: ActivityResultLauncher<Unit>

    fun closeScreen() = withOwner {
        finish()
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        withOwner {
            openAudioDocumentContract =
                registerForActivityResult(OpenAudioDocumentContract()) { audioUri ->
                    audioUri?.let {
                        contentResolver.takePersistableUriPermission(audioUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        onMusicChosen(audioUri)
                    }
                }
        }
    }

    fun openAudioDocumentScreen() = withOwner {
        openAudioDocumentContract.launch(Unit)
    }

    class Factory @Inject constructor() : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            GuidedBrushingSettingsNavigator() as T
    }
}

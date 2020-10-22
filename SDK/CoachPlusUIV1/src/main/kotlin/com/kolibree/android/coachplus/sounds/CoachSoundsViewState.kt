package com.kolibree.android.coachplus.sounds

import android.net.Uri

/**
 * Set the State of the coach sounds view
 */
internal data class CoachSoundsViewState(
    val actions: CoachSoundSettingsState = CoachSoundSettingsState.ACTION_NONE,
    val enableMusic: Boolean = false,
    val enableShuffle: Boolean = false,
    val enableTransitionSounds: Boolean = false,
    val musicURI: Uri = Uri.EMPTY
) {

    enum class CoachSoundSettingsState {
        ACTION_NONE,
        ACTION_CHOOSE_MUSIC
    }
}

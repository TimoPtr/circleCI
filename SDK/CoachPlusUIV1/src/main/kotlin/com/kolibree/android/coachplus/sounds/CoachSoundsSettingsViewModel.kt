/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.sounds

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kolibree.android.app.interactor.KolibreeServiceInteractor
import com.kolibree.android.coachplus.settings.BaseSettingsViewModel
import com.kolibree.android.coachplus.settings.persistence.repo.CoachSettingsRepository
import com.kolibree.android.coachplus.sounds.CoachSoundsViewState.CoachSoundSettingsState
import com.kolibree.android.sdk.e1.ToothbrushShutdownValve
import com.kolibree.sdkws.core.IKolibreeConnector
import javax.inject.Inject

/**
 * View model for the coach sounds userSettings page
 * Save the userSettings in the DB every time the user change a setting
 */
internal class CoachSoundsSettingsViewModel(
    settingsRepository: CoachSettingsRepository,
    connector: IKolibreeConnector,
    serviceInteractor: KolibreeServiceInteractor,
    shutdownValve: ToothbrushShutdownValve
) : BaseSettingsViewModel<CoachSoundsViewState>(
    settingsRepository,
    connector,
    serviceInteractor,
    shutdownValve
) {

    override fun initialViewState(): CoachSoundsViewState = CoachSoundsViewState(
        enableMusic = settings.enableMusic,
        enableShuffle = settings.enableShuffle,
        musicURI = settings.getUriOfMusic(),
        enableTransitionSounds = settings.enableTransitionSounds
    )

    fun onMusicChecked(enable: Boolean) {
        viewState = viewState.copy(enableMusic = enable)
        settings = settings.updateEnableMusic(enable)
        saveSettingsLocally()
    }

    fun onShuffleChecked(enable: Boolean) {
        viewState = viewState.copy(enableShuffle = enable)
        settings = settings.updateEnableShuffle(enable)
    }

    fun onTransitionSoundsChecked(enable: Boolean) {
        viewState = viewState.copy(enableTransitionSounds = enable)
        settings = settings.updateEnableTransitionSounds(enable)
        saveSettingsLocally()
    }

    fun onMusicChosen(uri: Uri) {
        settings = settings.updateMusicUri(uri.toString())
        viewState =
            viewState.copy(musicURI = settings.getUriOfMusic())
        emitViewState(viewState.copy(actions = CoachSoundSettingsState.ACTION_NONE))
        saveSettingsLocally()
    }

    fun chooseMusic() {
        emitViewState(viewState.copy(actions = CoachSoundSettingsState.ACTION_CHOOSE_MUSIC))
    }

    override fun createViewStateFromSettings() = CoachSoundsViewState(
        enableMusic = settings.enableMusic,
        enableShuffle = settings.enableShuffle,
        musicURI = settings.getUriOfMusic(),
        enableTransitionSounds = settings.enableTransitionSounds
    )

    class Factory
    @Inject
    constructor(
        private val settingsRepository: CoachSettingsRepository,
        private val connector: IKolibreeConnector,
        private val kolibreeServiceInteractor: KolibreeServiceInteractor,
        private val e1ShutdownValve: ToothbrushShutdownValve
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CoachSoundsSettingsViewModel(
                settingsRepository,
                connector,
                kolibreeServiceInteractor,
                e1ShutdownValve
            ) as T
        }
    }
}

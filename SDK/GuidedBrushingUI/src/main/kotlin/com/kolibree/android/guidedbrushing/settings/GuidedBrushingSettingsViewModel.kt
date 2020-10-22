/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.settings

import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.coachplus.settings.persistence.model.CoachSettings
import com.kolibree.android.coachplus.settings.persistence.repo.CoachSettingsRepository
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

internal class GuidedBrushingSettingsViewModel(
    initialViewState: GuidedBrushingSettingsViewState,
    private val navigator: GuidedBrushingSettingsNavigator,
    private val settingsRepository: CoachSettingsRepository,
    private val currentProfileProvider: CurrentProfileProvider
) : BaseViewModel<GuidedBrushingSettingsViewState, NoActions>(
    initialViewState
) {

    @VisibleForTesting
    var settings: CoachSettings = CoachSettings.create(currentProfileProvider.currentProfile().id)

    val isDisplayBrushingMovementOn: LiveData<Boolean> =
        mapNonNull(viewStateLiveData, initialViewState.isDisplayBrushingMovementOn) { viewState ->
            viewState.isDisplayBrushingMovementOn
        }

    val isDisplayHelpTextsOn: LiveData<Boolean> =
        mapNonNull(viewStateLiveData, initialViewState.isDisplayHelpTextsOn) { viewState ->
            viewState.isDisplayHelpTextsOn
        }

    val isMusicOn: LiveData<Boolean> =
        mapNonNull(viewStateLiveData, initialViewState.isMusicOn) { viewState ->
            viewState.isMusicOn
        }

    // using lazy here allow unit test otherwise Uri is throwing an exception
    val musicUriSelected: LiveData<Uri> by lazy {
        mapNonNull(viewStateLiveData, initialViewState.musicUri()) { viewState ->
            viewState.musicUri()
        }
    }

    val isTransitionSoundsOn: LiveData<Boolean> =
        mapNonNull(viewStateLiveData, initialViewState.isTransitionSoundsOn) { viewState ->
            viewState.isTransitionSoundsOn
        }

    fun onDisplayBrushingMovementClick(isOn: Boolean) {
        updateSettingsViewState { updateEnableBrushingMovement(isOn) }
        GuidedBrushingSettingsAnalytics.brushingMovements(isOn)
    }

    fun onDisplayHelpTextsClick(isOn: Boolean) {
        updateSettingsViewState { updateEnableHelpText(isOn) }
        GuidedBrushingSettingsAnalytics.helpTexts(isOn)
    }

    fun onMusicClick(isOn: Boolean) {
        updateSettingsViewState { updateEnableMusic(isOn) }
        GuidedBrushingSettingsAnalytics.music(isOn)
    }

    fun onTransitionSoundsClick(isOn: Boolean) {
        updateSettingsViewState { updateEnableTransitionSounds(isOn) }
        GuidedBrushingSettingsAnalytics.transitionSounds(isOn)
    }

    fun onChooseMusicClick() {
        GuidedBrushingSettingsAnalytics.chooseMusic()
        navigator.openAudioDocumentScreen()
    }

    fun onCloseClick() {
        GuidedBrushingSettingsAnalytics.goBack()
        navigator.closeScreen()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

        observeCurrentProfileSettings()
    }

    private fun observeCurrentProfileSettings() = disposeOnStop {
        currentProfileProvider.currentProfileFlowable()
            .subscribeOn(Schedulers.io())
            .switchMap { settingsRepository.getSettingsByProfileId(it.id) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::onNewCoachSettings, Timber::e)
    }

    private fun onNewCoachSettings(settings: CoachSettings) {
        updateSettingsViewState { settings }
    }

    private fun updateSettingsViewState(updateBlock: CoachSettings.() -> CoachSettings) {
        val updatedSettings = updateBlock(settings)
        updateViewState { withSettings(updatedSettings) }
        if (updatedSettings != settings) {
            settings = updatedSettings
            disposeOnCleared(::saveSettings)
        }
    }

    private fun saveSettings() = settingsRepository.save(settings)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({}, Timber::e)

    fun onMusicChosen(chosenMusicUri: Uri) {
        updateSettingsViewState { updateMusicUri(chosenMusicUri.toString()) }
    }

    class Factory @Inject constructor(
        private val navigator: GuidedBrushingSettingsNavigator,
        private val settingsRepository: CoachSettingsRepository,
        private val currentProfileProvider: CurrentProfileProvider
    ) : BaseViewModel.Factory<GuidedBrushingSettingsViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            GuidedBrushingSettingsViewModel(
                viewState ?: GuidedBrushingSettingsViewState.initial(),
                navigator,
                settingsRepository,
                currentProfileProvider
            ) as T
    }
}

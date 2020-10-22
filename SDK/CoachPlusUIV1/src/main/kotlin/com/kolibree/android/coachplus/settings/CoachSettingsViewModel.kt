/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kolibree.android.app.interactor.KolibreeServiceInteractor
import com.kolibree.android.coachplus.settings.CoachSettingsViewState.CoachSettingsState
import com.kolibree.android.coachplus.settings.persistence.repo.CoachSettingsRepository
import com.kolibree.android.sdk.e1.ToothbrushShutdownValve
import com.kolibree.sdkws.core.IKolibreeConnector
import javax.inject.Inject

/**
 * View model for the coach sounds userSettings page
 * Save the userSettings in the DB every time the user change a setting
 */
internal class CoachSettingsViewModel(
    settingsRepository: CoachSettingsRepository,
    connector: IKolibreeConnector,
    serviceInteractor: KolibreeServiceInteractor,
    shutdownValve: ToothbrushShutdownValve
) : BaseSettingsViewModel<CoachSettingsViewState>(
    settingsRepository,
    connector,
    serviceInteractor,
    shutdownValve
) {

    override fun initialViewState(): CoachSettingsViewState {
        return CoachSettingsViewState(
            enableBrushingMovement = settings.enableBrushingMovement,
            enableHelpText = settings.enableHelpText
        )
    }

    fun onDisplayBrushingMovementChecked(enable: Boolean) {
        settings = settings.updateEnableBrushingMovement(enable)
        viewState = viewState.copy(enableBrushingMovement = enable)
        saveSettingsLocally()
    }

    fun onDisplayHelpTextChecked(enable: Boolean) {
        settings = settings.updateEnableHelpText(enable)
        viewState = viewState.copy(enableHelpText = enable)
        saveSettingsLocally()
    }

    fun openSoundPage() {
        emitViewState(viewState.copy(actions = CoachSettingsState.ACTION_OPEN_SOUND_PAGE))
    }

    override fun createViewStateFromSettings(): CoachSettingsViewState {
        return CoachSettingsViewState(
            enableBrushingMovement = settings.enableBrushingMovement,
            enableHelpText = settings.enableHelpText
        )
    }

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
            return CoachSettingsViewModel(
                settingsRepository,
                connector,
                kolibreeServiceInteractor,
                e1ShutdownValve
            ) as T
        }
    }
}

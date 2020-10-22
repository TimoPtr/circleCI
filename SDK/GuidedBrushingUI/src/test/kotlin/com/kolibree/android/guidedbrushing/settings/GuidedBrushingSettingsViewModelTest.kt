/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.settings

import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.coachplus.settings.persistence.model.CoachSettings
import com.kolibree.android.coachplus.settings.persistence.repo.CoachSettingsRepository
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.tracker.AnalyticsEvent
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@Suppress("TooManyFunctions")
internal class GuidedBrushingSettingsViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: GuidedBrushingSettingsViewModel

    private val navigator: GuidedBrushingSettingsNavigator = mock()

    private val settingsRepository: CoachSettingsRepository = mock()

    private val profileProvider: CurrentProfileProvider = mock()

    override fun setup() {
        super.setup()

        whenever(profileProvider.currentProfile())
            .thenReturn(ProfileBuilder.create().build())
        whenever(settingsRepository.save(any()))
            .thenReturn(Completable.complete())

        viewModel = GuidedBrushingSettingsViewModel(
            initialViewState = GuidedBrushingSettingsViewState.initial(),
            navigator = navigator,
            settingsRepository = settingsRepository,
            currentProfileProvider = profileProvider
        )
    }

    /*
    onDisplayBrushingMovementClick
     */

    @Test
    fun `onDisplayBrushingMovementClick updates viewState`() {
        viewModel.onDisplayBrushingMovementClick(true)
        assertTrue(viewModel.getViewState()!!.isDisplayBrushingMovementOn)

        viewModel.onDisplayBrushingMovementClick(false)
        assertFalse(viewModel.getViewState()!!.isDisplayBrushingMovementOn)
    }

    @Test
    fun `onDisplayBrushingMovementClick saves updated settings`() {
        viewModel.settings = CoachSettings.create(ProfileBuilder.DEFAULT_ID)
            .updateEnableBrushingMovement(false)
        val settingsBefore = viewModel.settings

        viewModel.onDisplayBrushingMovementClick(true)

        val expectedSettings = settingsBefore.updateEnableBrushingMovement(true)
        verify(settingsRepository).save(expectedSettings)
    }

    @Test
    fun `onDisplayBrushingMovementClick sends Analytics event`() {
        viewModel.onDisplayBrushingMovementClick(true)
        verify(eventTracker).sendEvent(AnalyticsEvent("GBSetting_BrushingMoveDisplay_On"))

        viewModel.onDisplayBrushingMovementClick(false)
        verify(eventTracker).sendEvent(AnalyticsEvent("GBSetting_BrushingMoveDisplay_Off"))
    }

    /*
    onDisplayHelpTextsClick
     */

    @Test
    fun `onDisplayHelpTextsClick updates viewState`() {
        viewModel.onDisplayHelpTextsClick(true)
        assertTrue(viewModel.getViewState()!!.isDisplayHelpTextsOn)

        viewModel.onDisplayHelpTextsClick(false)
        assertFalse(viewModel.getViewState()!!.isDisplayHelpTextsOn)
    }

    @Test
    fun `onDisplayHelpTextsClick saves updated settings`() {
        viewModel.settings = CoachSettings.create(ProfileBuilder.DEFAULT_ID)
            .updateEnableHelpText(false)
        val settingsBefore = viewModel.settings

        viewModel.onDisplayHelpTextsClick(true)

        val expectedSettings = settingsBefore.updateEnableHelpText(true)
        verify(settingsRepository).save(expectedSettings)
    }

    @Test
    fun `onDisplayHelpTextsClick sends Analytics event`() {
        viewModel.onDisplayHelpTextsClick(true)
        verify(eventTracker).sendEvent(AnalyticsEvent("GBSetting_LiveFeedbackDisplay_On"))

        viewModel.onDisplayHelpTextsClick(false)
        verify(eventTracker).sendEvent(AnalyticsEvent("GBSetting_LiveFeedbackDisplay_Off"))
    }

    /*
    onMusicClick
     */

    @Test
    fun `onMusicClick updates viewState`() {
        viewModel.onMusicClick(true)
        assertTrue(viewModel.getViewState()!!.isMusicOn)

        viewModel.onMusicClick(false)
        assertFalse(viewModel.getViewState()!!.isMusicOn)
    }

    @Test
    fun `onMusicClick saves updated settings`() {
        viewModel.settings = CoachSettings.create(ProfileBuilder.DEFAULT_ID)
            .updateEnableMusic(false)
        val settingsBefore = viewModel.settings

        viewModel.onMusicClick(true)

        val expectedSettings = settingsBefore.updateEnableMusic(true)
        verify(settingsRepository).save(expectedSettings)
    }

    @Test
    fun `onMusicClick sends Analytics event`() {
        viewModel.onMusicClick(true)
        verify(eventTracker).sendEvent(AnalyticsEvent("GBSetting_Music_On"))

        viewModel.onMusicClick(false)
        verify(eventTracker).sendEvent(AnalyticsEvent("GBSetting_Music_Off"))
    }

    /*
    onTransitionSoundsClick
     */

    @Test
    fun `onTransitionSoundsClick updates viewState`() {
        viewModel.onTransitionSoundsClick(true)
        assertTrue(viewModel.getViewState()!!.isTransitionSoundsOn)

        viewModel.onTransitionSoundsClick(false)
        assertFalse(viewModel.getViewState()!!.isTransitionSoundsOn)
    }

    @Test
    fun `onTransitionSoundsClick saves updated settings`() {
        viewModel.settings = CoachSettings.create(ProfileBuilder.DEFAULT_ID)
            .updateEnableTransitionSounds(false)
        val settingsBefore = viewModel.settings

        viewModel.onTransitionSoundsClick(true)

        val expectedSettings = settingsBefore.updateEnableTransitionSounds(true)
        verify(settingsRepository).save(expectedSettings)
    }

    @Test
    fun `onTransitionSoundsClick sends Analytics event`() {
        viewModel.onTransitionSoundsClick(true)
        verify(eventTracker).sendEvent(AnalyticsEvent("GBSetting_SoundTransition_On"))

        viewModel.onTransitionSoundsClick(false)
        verify(eventTracker).sendEvent(AnalyticsEvent("GBSetting_SoundTransition_Off"))
    }

    /*
    onChooseMusicClick
     */

    @Test
    fun `onChooseMusicClick navigates to choose music screen`() {
        viewModel.onChooseMusicClick()

        verify(navigator).openAudioDocumentScreen()
    }

    @Test
    fun `onChooseMusicClick sends Analytics event`() {
        viewModel.onChooseMusicClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("GBSetting_ChooseMusic"))
    }

    /*
    onCloseClick
     */

    @Test
    fun `onCloseClick closes screen`() {
        viewModel.onCloseClick()

        verify(navigator).closeScreen()
    }

    @Test
    fun `onCloseClick sends Analytics event`() {
        viewModel.onCloseClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("GBSetting_GoBack"))
    }
}

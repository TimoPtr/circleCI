/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.mvi

import android.graphics.Color
import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import com.google.common.base.Optional
import com.jraska.livedata.test
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.interactor.GameInteractor
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.widget.ZoneData
import com.kolibree.android.app.ui.widget.ZoneProgressData
import com.kolibree.android.coachplus.CoachPlusAnalytics
import com.kolibree.android.coachplus.controller.CoachPlusController
import com.kolibree.android.coachplus.controller.CoachPlusControllerResult
import com.kolibree.android.coachplus.controller.SupervisionInfo
import com.kolibree.android.coachplus.feedback.FeedBackMessage
import com.kolibree.android.coachplus.settings.persistence.model.CoachSettings
import com.kolibree.android.coachplus.settings.persistence.repo.CoachSettingsRepository
import com.kolibree.android.coachplus.ui.CoachPlusColorSet
import com.kolibree.android.coachplus.ui.colors.CurrentZoneColorProvider
import com.kolibree.android.coachplus.utils.RingLedColorUseCase
import com.kolibree.android.coachplus.utils.ZoneHintProvider
import com.kolibree.android.commons.MIN_BRUSHING_DURATION_SECONDS
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.extensions.plusAssign
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.feature.CoachPlusPlaqlessSupervisionFeature
import com.kolibree.android.game.BrushingCreator
import com.kolibree.android.game.KeepScreenOnController
import com.kolibree.android.game.bi.Contract.ActivityName.COACH_PLUS
import com.kolibree.android.game.bi.KmlAvroCreator
import com.kolibree.android.game.sensors.interactors.GameToothbrushInteractorFacade
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import com.kolibree.android.sdk.connection.brushingmode.BrushingModeManager
import com.kolibree.android.sdk.connection.brushingmode.ConfirmBrushingModeUseCase
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush
import com.kolibree.android.sdk.connection.vibrator.Vibrator
import com.kolibree.android.sdk.disconnection.LostConnectionHandler
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.mockFacadeWithLifecycleSupport
import com.kolibree.android.test.utils.ReflectionUtils
import com.kolibree.android.test.utils.TestFeatureToggle
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.kml.BrushingSession
import com.kolibree.kml.CharVector
import com.kolibree.kml.MouthZone16
import com.kolibree.sdkws.core.IKolibreeConnector
import com.kolibree.sdkws.core.ProfileWrapper
import com.kolibree.sdkws.data.model.CreateBrushingData
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.CompletableSubject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.threeten.bp.Duration
import timber.log.Timber

internal class CoachPlusViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: CoachPlusViewModel

    @Mock
    private lateinit var gameInteractor: GameInteractor

    private val gameToothbrushInteractorFacade: GameToothbrushInteractorFacade =
        mockFacadeWithLifecycleSupport()

    @Mock
    private lateinit var lostConnectionHandler: LostConnectionHandler

    @Mock
    private lateinit var soundInteractor: SoundInteractor

    @Mock
    private lateinit var colorSet: CoachPlusColorSet

    @Mock
    private lateinit var connector: IKolibreeConnector

    @Mock
    private lateinit var coachPlusController: CoachPlusController

    @Mock
    private lateinit var coachSettingsRepository: CoachSettingsRepository

    @Mock
    private lateinit var zoneHintProvider: ZoneHintProvider

    @Mock
    private lateinit var confirmBrushingModeUseCase: ConfirmBrushingModeUseCase

    @Mock
    private lateinit var ringLedColorUseCase: RingLedColorUseCase

    @Mock
    private lateinit var brushingCreator: BrushingCreator

    @Mock
    private lateinit var keepScreenOnController: KeepScreenOnController

    @Mock
    private lateinit var colorProvider: CurrentZoneColorProvider

    @Mock
    private lateinit var kmlAvroCreator: KmlAvroCreator

    private val plaqlesssSupervisionFeature = TestFeatureToggle(CoachPlusPlaqlessSupervisionFeature)

    private val featuresToggle = setOf(
        plaqlesssSupervisionFeature
    )

    override fun setup() {
        super.setup()
        FailEarly.overrideDelegateWith(NoopTestDelegate)

        whenever(zoneHintProvider.provideHintForZone(any())).thenReturn(0)

        val settings = mock<CoachSettings>()
        val uri = mock<Uri>()

        whenever(settings.enableHelpText).thenReturn(false)
        whenever(settings.enableTransitionSounds).thenReturn(false)
        whenever(settings.enableBrushingMovement).thenReturn(false)
        whenever(settings.enableMusic).thenReturn(true)
        whenever(settings.getUriOfMusic()).thenReturn(uri)

        whenever(coachSettingsRepository.getSettingsByProfileId(any())).thenReturn(
            Flowable.just(
                settings
            )
        )

        val profile: Profile = mock()
        val profileWrapper: ProfileWrapper = mock()
        whenever(profile.id).thenReturn(1L)
        whenever(connector.currentProfile).thenReturn(profile)
        whenever(connector.currentProfile!!.id).thenReturn(1L)
        whenever(connector.withProfileId(any())).thenReturn(profileWrapper)

        whenever(coachPlusController.zoneChangeObservable).thenReturn(Observable.empty())

        whenever(lostConnectionHandler.connectionObservable(anyString()))
            .thenReturn(Observable.empty())

        whenever(ringLedColorUseCase.getRingLedColor(any())).thenReturn(Flowable.empty())
    }

    @Before
    fun before() {
        viewModel = createViewModel()
    }

    override fun tearDown() {
        super.tearDown()

        try {
            ReflectionUtils.invokeProtectedVoidMethod(viewModel, "onCleared")
        } catch (_: Throwable) {
            // Was outputting too much red lines
        }
    }

    @Test
    fun `isManual returns true when macAddress is null`() {
        assertTrue(viewModel.isManual)
    }

    @Test
    fun `isManual returns false when macAddress is not null`() {
        viewModel = createViewModel("hello")
        assertFalse(viewModel.isManual)
    }

    @Test
    fun `initialViewState in manual mode`() {
        val vs = viewModel.getViewState()
        assertNotNull(vs)
        assertTrue(vs!!.isManual)
        assertFalse(vs.isInit)
        assertFalse(vs.isPlaying)
    }

    @Test
    fun `coachPlusTitleColor equals to colorSet titleColor`() {
        val expectedColor = Color.BLACK
        whenever(colorSet.titleColor).thenReturn(expectedColor)

        viewModel = createViewModel()

        assertEquals(expectedColor, viewModel.coachPlusTitleColor)
    }

    @Test
    fun `coachPlusViewBackgroundColor equals to colorSet backgroundColor`() {
        val expectedColor = Color.BLACK
        whenever(colorSet.backgroundColor).thenReturn(expectedColor)

        viewModel = createViewModel()

        assertEquals(expectedColor, viewModel.coachPlusViewBackgroundColor)
    }

    @Test
    fun `brushingProgramAvailable returns true if tbmodel support vibration speed update`() {
        viewModel = createViewModel(model = ToothbrushModel.CONNECT_E2)

        assertTrue(viewModel.brushingProgramAvailable)
    }

    @Test
    fun `brushingProgramAvailable returns false if tbmodel does not support vibration speed update or isManual`() {
        // isManual = true
        assertFalse(viewModel.brushingProgramAvailable)

        viewModel = createViewModel(model = ToothbrushModel.ARA)

        assertFalse(viewModel.brushingProgramAvailable)
    }

    @Test
    fun `isPlaying emits the value of viewState isPlaying`() {
        val testObserver = viewModel.isPlaying.test()

        viewModel.updateViewState { CoachPlusViewState(true, isPlaying = true) }

        testObserver.assertValue(true)

        viewModel.updateViewState { CoachPlusViewState(true, isPlaying = false) }

        testObserver.assertValue(false)
    }

    @Test
    fun `shouldShowToothbrushHead emits the value of viewState shouldShowToothbrushHead`() {
        val testObserver = viewModel.shouldShowToothbrushHead.test()

        viewModel.updateViewState {
            CoachPlusViewState(
                true,
                isPlaying = true,
                isBrushingMovementEnabled = true
            )
        }

        testObserver.assertValue(true)

        viewModel.updateViewState {
            CoachPlusViewState(
                true,
                isPlaying = false,
                isBrushingMovementEnabled = true
            )
        }

        testObserver.assertValue(false)
    }

    @Test
    fun `ringLedColor emits the value of viewState ringLedColor`() {
        val testObserver = viewModel.ringLedColor.test()
        val expectedColor = Color.YELLOW

        viewModel.updateViewState { CoachPlusViewState(false, ringLedColor = expectedColor) }

        testObserver.assertValue(expectedColor)
    }

    @Test
    fun `currentZone emits the value of viewState currentZone`() {
        val testObserver = viewModel.currentZone.test()
        val expectedZone = MouthZone16.LoIncInt

        viewModel.updateViewState { CoachPlusViewState(false, currentZone = expectedZone) }

        testObserver.assertValue(expectedZone)
    }

    @Test
    fun `currentZoneColor emits the return value of colorProvider evaluateMaterialColor with currentZoneProgress viewState currentZoneColor`() {
        val testObserver = viewModel.currentZoneColor.test()
        val currentProgress = 21
        val expectedZoneColor = Color.CYAN

        whenever(colorProvider.provideCurrentZoneColor(any())).thenReturn(
            expectedZoneColor
        )

        viewModel.updateViewState {
            CoachPlusViewState(
                false,
                currentZoneProgress = currentProgress
            )
        }

        testObserver.assertValue(expectedZoneColor)
    }

    /*
    progressPercentage
     */

    @Test
    fun `progressPercentage emits view state's currentZoneProgress when plaqueLevelPercent is null`() {
        val testObserver = viewModel.progressPercentage.test()
        val currentProgress = 21
        val plaqueLevel: Int? = null

        viewModel.updateViewState {
            CoachPlusViewState(
                isManual = false,
                currentZoneProgress = currentProgress
            )
        }

        testObserver.assertValue(currentProgress)
    }

    @Test
    fun `borderProgressColor emits WHITE when the value of viewState isBrushingGoodZone is true`() {
        val testObserver = viewModel.borderProgressColor.test()

        viewModel.updateViewState { CoachPlusViewState(false, isBrushingGoodZone = true) }

        testObserver.assertValue(Color.WHITE)
    }

    @Test
    fun `borderProgressColor emits RED when the value of viewState isBrushingGoodZone is false`() {
        val testObserver = viewModel.borderProgressColor.test()

        viewModel.updateViewState { CoachPlusViewState(false, isBrushingGoodZone = false) }

        testObserver.assertValue(Color.RED)
    }

    @Test
    fun `hint emits the return value of viewState getHint`() {
        val testObserver = viewModel.hint.test()

        viewModel.updateViewState {
            CoachPlusViewState(
                false,
                isBrushingGoodZone = false,
                isHelpTextEnabled = true
            )
        }

        testObserver.assertValue(zoneHintProvider.provideHintForWrongZone())
    }

    @Test
    fun `feedback emits the value of viewState optionalFeedback`() {
        val testObserver = viewModel.feedback.test()

        viewModel.updateViewState {
            CoachPlusViewState(
                false,
                isPlaying = true,
                feedBackMessage = FeedBackMessage.WrongIncisorsIntAngleFeedback
            )
        }

        testObserver.assertValue(FeedBackMessage.WrongIncisorsIntAngleFeedback)

        viewModel.updateViewState {
            CoachPlusViewState(
                false,
                isPlaying = false,
                feedBackMessage = FeedBackMessage.WrongIncisorsIntAngleFeedback
            )
        }

        testObserver.assertValue(FeedBackMessage.EmptyFeedback)
    }

    @Test
    fun `shouldShowPlay emits the value of viewState not isInit and isManual`() {
        val testObserver = viewModel.shouldShowPlay.test()

        viewModel.updateViewState {
            CoachPlusViewState(
                true,
                isInit = false
            )
        }

        testObserver.assertValue(true)

        viewModel.updateViewState {
            CoachPlusViewState(
                true,
                isInit = true
            )
        }

        testObserver.assertValue(false)
    }

    @Test
    fun `shouldShowPause emits the value of viewState shouldShowPause`() {
        val testObserver = viewModel.shouldShowPause.test()

        viewModel.updateViewState {
            CoachPlusViewState(
                false,
                isInit = true,
                isPlaying = false
            )
        }

        testObserver.assertValue(true)

        viewModel.updateViewState {
            CoachPlusViewState(
                false,
                isInit = true,
                isPlaying = true
            )
        }

        testObserver.assertValue(false)
    }

    @Test
    fun `isPlayingStream maps isPlaying from view state`() {
        val testObserver = viewModel.isPlaying.test()

        viewModel.updateViewState { CoachPlusViewState(true, isPlaying = true) }

        testObserver.assertValue(true)
    }

    @Test
    fun `onCreate initialises all needed object`() {
        val owner: LifecycleOwner = mock()

        viewModel.onCreate(owner)

        verify(viewModel).loadSettings()
        verify(viewModel).initTicker()
        verify(viewModel).initTransitionDetection()
        verify(soundInteractor).setLifecycleOwner(eq(owner))
        verify(brushingCreator).setLifecycleOwner(eq(owner))

        verify(brushingCreator).addListener(eq(viewModel))
    }

    @Test
    fun `onStart invokes initAmbientSound`() {
        doNothing().whenever(viewModel).initAmbientSound()
        whenever(gameToothbrushInteractorFacade.gameLifeCycleObservable())
            .thenReturn(Observable.empty())

        viewModel.onStart(mock())

        verify(viewModel).initAmbientSound()
    }

    @Test
    fun `onPause invokes stopCoachPlus`() {
        doNothing().whenever(viewModel).stopCoachPlus(anyBoolean())

        viewModel.onPause(mock())

        verify(viewModel).stopCoachPlus(false)
    }

    @Test
    fun `onDestroy invokes disposables dispose and brushingCreator removeListener `() {
        val subscription = Observable.never<Boolean>().subscribe({}, Timber::e)
        viewModel.disposables += subscription
        doNothing().whenever(brushingCreator).removeListener(any())

        viewModel.onDestroy(mock())

        assertTrue(subscription.isDisposed)
    }

    @Test
    fun `onConnectionEstablished never invokes startCoachPlus`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().withVibration(false).build()
        val toothbrush = mock<Toothbrush>()
        doNothing().whenever(gameToothbrushInteractorFacade).onConnectionEstablished(eq(connection))

        whenever(connection.toothbrush()).thenReturn(toothbrush)
        whenever(toothbrush.model).thenReturn(ToothbrushModel.ARA)

        viewModel.onConnectionEstablished(connection)

        verify(viewModel, never()).startCoachPlus()
    }

    @Test
    fun `onConnectionEstablished never invokes facade onConnectionEstablished`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().withVibration(false).build()
        val toothbrush = mock<Toothbrush>()
        doNothing().whenever(gameToothbrushInteractorFacade).onConnectionEstablished(eq(connection))

        whenever(connection.toothbrush()).thenReturn(toothbrush)
        whenever(toothbrush.model).thenReturn(ToothbrushModel.ARA)

        viewModel.onConnectionEstablished(connection)

        verify(gameToothbrushInteractorFacade, never()).onConnectionEstablished(eq(connection))
    }

    @Test
    fun `onConnectionEstablished invokes listenToRingLedChange and sendSupervisionInfo when model is PLAQLESS`() {
        val connection =
            KLTBConnectionBuilder.createAndroidLess().withModel(ToothbrushModel.PLAQLESS)
                .withVibration(false).build()
        doNothing().whenever(gameToothbrushInteractorFacade).onConnectionEstablished(eq(connection))
        doNothing().whenever(viewModel).listenToRingLedChange(eq(connection))
        doNothing().whenever(viewModel).sendSupervisionInfo(eq(connection))

        viewModel.onConnectionEstablished(connection)

        verify(viewModel).listenToRingLedChange(eq(connection))
        verify(viewModel).sendSupervisionInfo(eq(connection))
        verify(viewModel, never()).startCoachPlus()
    }

    @Test
    fun `onConnectionEstablished invokes startCoachhPlus when vibrator isOn`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().withVibration(true).build()
        doNothing().whenever(gameToothbrushInteractorFacade).onConnectionEstablished(eq(connection))
        doNothing().whenever(viewModel).listenToRingLedChange(eq(connection))
        doNothing().whenever(viewModel).sendSupervisionInfo(eq(connection))
        doNothing().whenever(viewModel).startCoachPlus()

        viewModel.onConnectionEstablished(connection)

        verify(viewModel).startCoachPlus()
    }

    @Test
    fun `onLostConnectionHandleStateChanged invokes stopCoachPlus when state is not CONNECTION ACTIVE`() {
        doNothing().whenever(viewModel).stopCoachPlus(anyBoolean())

        viewModel.onLostConnectionHandleStateChanged(
            mock(),
            LostConnectionHandler.State.CONNECTION_ACTIVE
        )

        verify(viewModel, never()).stopCoachPlus(anyBoolean())

        LostConnectionHandler.State.values()
            .filter { it != LostConnectionHandler.State.CONNECTION_ACTIVE }
            .forEach { value ->
                viewModel.onLostConnectionHandleStateChanged(mock(), value)
            }

        verify(viewModel, times(2)).stopCoachPlus(false)
    }

    @Test
    fun `onLostConnectionHandleStateChanged invokes notifyReconnection when state is CONNECTION ACTIVE`() {
        viewModel.onLostConnectionHandleStateChanged(
            mock(),
            LostConnectionHandler.State.CONNECTION_ACTIVE
        )

        verify(coachPlusController).notifyReconnection()
    }

    @Test
    fun `onLostConnectionHandleStateChanged never invokes notifyReconnection when state is NOT CONNECTION ACTIVE`() {
        doNothing().whenever(viewModel).stopCoachPlus(anyBoolean())

        LostConnectionHandler.State.values()
            .filter { it != LostConnectionHandler.State.CONNECTION_ACTIVE }
            .forEach { value ->
                viewModel.onLostConnectionHandleStateChanged(mock(), value)
            }

        verify(coachPlusController, never()).notifyReconnection()
    }

    @Test
    fun `onVibratorOn invokes startCoachPlus`() {
        doNothing().whenever(viewModel).startCoachPlus()
        viewModel.onVibratorOn(mock())
        verify(viewModel).startCoachPlus()
    }

    @Test
    fun `onVibratorOff invokes stopCoachPlus`() {
        doNothing().whenever(viewModel).stopCoachPlus(anyBoolean())
        viewModel.onVibratorOff(mock())
        verify(viewModel).stopCoachPlus(false)
    }

    @Test
    fun `onSuccessfullySentData pushes DataSaved action`() {
        val testObserver = viewModel.actionsObservable.test()

        viewModel.onSuccessfullySentData()

        testObserver.assertValue(CoachPlusActions.DataSaved)
    }

    @Test
    fun `somethingWrong pushes SomethingWrong action`() {
        val testObserver = viewModel.actionsObservable.test()
        val expectedException = Exception()
        viewModel.somethingWrong(expectedException)

        testObserver.assertValue(CoachPlusActions.SomethingWrong(expectedException))
    }

    @Test
    fun `onQuitButtonClick invokes onGameFinishing`() {
        doNothing().whenever(viewModel).onGameFinishing()
        viewModel.onQuitButtonClick()

        verify(viewModel).onGameFinishing()
    }

    /*
    onRestartButtonClick
     */

    @Test
    fun `onRestartButtonClick invokes publishAvro only when is not manual`() {
        val transitionTable = IntArray(0)

        doNothing().whenever(coachPlusController).reset()
        doNothing().whenever(viewModel).resumeCoachPlus()

        viewModel.onRestartButtonClick()
        doNothing().whenever(viewModel).publishAvro()

        whenever(coachPlusController.getAvroTransitionsTable()).thenReturn(transitionTable)

        viewModel = createViewModel("hello")

        viewModel.onRestartButtonClick()

        verify(viewModel).publishAvro()
    }

    @Test
    fun `onRestartButtonClick invokes coachPlusController reset, push action Restarted and invokes resumeCoachPlus`() {
        doNothing().whenever(coachPlusController).reset()
        doNothing().whenever(viewModel).resumeCoachPlus()

        val testObserver = viewModel.actionsObservable.test()

        viewModel.onRestartButtonClick()

        verify(coachPlusController).reset()
        verify(viewModel).resumeCoachPlus()

        testObserver.assertValue(CoachPlusActions.Restarted)
    }

    @Test
    fun `onRestartButtonClick invokes nullifyCurrentZone`() {
        doNothing().whenever(coachPlusController).reset()
        doNothing().whenever(viewModel).resumeCoachPlus()

        viewModel.onRestartButtonClick()

        verify(viewModel).nullifyCurrentZone()
    }

    @Test
    fun `onRestartButtonClick notify the restart observable`() {
        val test = viewModel.restartStream.test()

        viewModel.onRestartButtonClick()
        assertEquals(1, test.valueCount())

        viewModel.onRestartButtonClick()
        assertEquals(2, test.valueCount())
    }

    @Test
    fun `restart restores default brushing zones`() {
        val defaultBrushingZones = guidedBrushingZones()
        val mockBrushingZones = ZoneProgressData(
            zones = (0..10).map { ZoneData(true, it / 10f) }
        )

        assertNotEquals(defaultBrushingZones, mockBrushingZones)

        viewModel = createViewModel(
            viewState = CoachPlusViewState(
                isManual = true,
                zoneProgressData = mockBrushingZones
            )
        )

        viewModel.onRestartButtonClick()

        val viewState = viewModel.getViewState()!!
        assertEquals(viewState.zoneProgressData, defaultBrushingZones)
    }

    @Test
    fun `onResumeButtonClick invokes resumeCoachPlus`() {
        doNothing().whenever(viewModel).resumeCoachPlus()

        viewModel.onResumeButtonClick()

        verify(viewModel).onResumeButtonClick()
    }

    /*
    onSettingsButtonClick
     */

    @Test
    fun `onSettingsButtonClick pushes OpenSettings action with toothbrush mac from GameInteractor`() {
        val testObserver = viewModel.actionsObservable.test()

        val expectedMac = "dasda"
        whenever(gameInteractor.toothbrushMac).thenReturn(expectedMac)

        viewModel.onSettingsButtonClick()

        testObserver.assertValue(CoachPlusActions.OpenSettings(expectedMac))
    }

    @Test
    fun `onSettingsButtonClick pushes OpenSettings action with null mac if GameInteractor doesn't have a mac yet`() {
        val testObserver = viewModel.actionsObservable.test()

        viewModel.onSettingsButtonClick()

        testObserver.assertValue(CoachPlusActions.OpenSettings(null))
    }

    /*
    onManualPause
     */

    @Test
    fun `onManualPause invokes stopCoachPlus only when isManual true`() {
        viewModel = createViewModel("hello")

        viewModel.onManualPause()

        verify(viewModel, never()).stopCoachPlus(anyBoolean())

        viewModel = createViewModel()
        doNothing().whenever(viewModel).stopCoachPlus(false)

        viewModel.onManualPause()

        verify(viewModel).stopCoachPlus(false)
    }

    @Test
    fun `onManualStart invokes startCoachPlus only when isManual true`() {
        viewModel = createViewModel("hello")

        viewModel.onManualStart()

        verify(viewModel, never()).startCoachPlus()

        viewModel = createViewModel()
        doNothing().whenever(viewModel).startCoachPlus()

        viewModel.onManualStart()

        verify(viewModel).startCoachPlus()
    }

    @Test
    fun `onBrushingProgramButtonClick invokes stopCoachPlus and push ShowBrushingModeDialog action`() {
        doNothing().whenever(viewModel).stopCoachPlus(anyBoolean())
        val testObserver = viewModel.actionsObservable.test()
        val connection = mock<KLTBConnection>()
        val brushingModeManager = mock<BrushingModeManager>()
        val modes = mock<List<BrushingMode>>()
        val currentMode = mock<BrushingMode>()

        whenever(gameInteractor.connection).thenReturn(connection)
        whenever(connection.brushingMode()).thenReturn(brushingModeManager)
        whenever(brushingModeManager.availableBrushingModes()).thenReturn(Single.just(modes))
        whenever(brushingModeManager.getCurrent()).thenReturn(Single.just(currentMode))
        viewModel.onBrushingProgramButtonClick()

        testObserver.assertValue(CoachPlusActions.ShowBrushingModeDialog(modes, currentMode))
        verify(viewModel).stopCoachPlus(false)
    }

    @Test
    fun `onBrushingModeSelected invokes confirmBrushingModeUseCase confirmBrushingModeCompletable`() {
        val profile = mock<Profile>()
        val profileId = 1L
        val brushingMode = BrushingMode.defaultMode()

        whenever(connector.currentProfile).thenReturn(profile)
        whenever(profile.id).thenReturn(profileId)
        whenever(
            confirmBrushingModeUseCase.confirmBrushingModeCompletable(
                eq(profileId),
                eq(brushingMode)
            )
        ).thenReturn(
            Completable.complete()
        )

        viewModel.onBrushingModeSelected(brushingMode)

        verify(confirmBrushingModeUseCase).confirmBrushingModeCompletable(
            eq(profileId),
            eq(brushingMode)
        )
    }

    @Test
    fun `onBackPressed invokes stopCoachPlus when the game was initialized`() {
        doNothing().whenever(viewModel).stopCoachPlus(anyBoolean())

        viewModel.updateViewState {
            CoachPlusViewState(
                isManual = false,
                isInit = true,
                isPlaying = true
            )
        }

        viewModel.onBackPressed()

        verify(viewModel).stopCoachPlus(false)
    }

    @Test
    fun `onBackPressed pushes CoachPlusActionsCancel action when the game was not initialized`() {
        viewModel.updateViewState {
            CoachPlusViewState(
                isManual = false,
                isInit = false,
                isPlaying = false
            )
        }

        val testObserver = viewModel.actionsObservable.test()

        viewModel.onBackPressed()

        testObserver.assertValue(CoachPlusActions.Cancel)
        verify(viewModel, never()).stopCoachPlus(anyBoolean())
    }

    @Test
    fun `onBackPressed calls onGameFinishing action when the game was initialized and paused`() {
        viewModel.updateViewState {
            CoachPlusViewState(
                isManual = false,
                isInit = true,
                isPlaying = false
            )
        }

        viewModel.onBackPressed()

        verify(viewModel).onGameFinishing()
        verify(viewModel, never()).stopCoachPlus(false)
    }

    @Test
    fun `loadSettings invokes coachSettingsRepo getSettings and push to settingsRelay and updateViewState`() {
        val profile = mock<Profile>()
        val profileId = 1L
        val settings = mock<CoachSettings>()

        whenever(connector.currentProfile).thenReturn(profile)
        whenever(profile.id).thenReturn(profileId)
        whenever(settings.enableBrushingMovement).thenReturn(true)
        whenever(settings.enableHelpText).thenReturn(true)
        whenever(coachSettingsRepository.getSettingsByProfileId(eq(profileId))).thenReturn(
            Flowable.just(
                settings
            )
        )

        viewModel.loadSettings()

        verify(coachSettingsRepository).getSettingsByProfileId(eq(profileId))

        viewModel.settingsRelay.test().assertValue(settings)
        viewModel.viewStateFlowable.test().assertValue { vs ->
            vs.isBrushingMovementEnabled && vs.isHelpTextEnabled
        }
    }

    @Test
    fun `initTicker invokes onTick when isPlaying`() {
        viewModel =
            createViewModel("hello", viewState = CoachPlusViewState(false, isPlaying = true))
        doNothing().whenever(viewModel).onTick(any())
        whenever(viewModel.tickerObservable).thenReturn(Observable.just(1))

        viewModel.initTicker()

        verify(viewModel).onTick(any())
    }

    @Test
    fun `initTransitionDetection invokes soundInteractors as many zoneChangeObservable emit and enableTransitionSounds true`() {
        val settings = mock<CoachSettings>()

        viewModel.settingsRelay.accept(settings)
        whenever(settings.enableTransitionSounds).thenReturn(true, false)
        doNothing().whenever(soundInteractor).playTransitionSound()

        val supervisedInfos = listOf(
            SupervisionInfo(
                MouthZone16.LoIncInt,
                1
            ), SupervisionInfo(
                MouthZone16.LoMolLeExt,
                2
            )
        )

        whenever(coachPlusController.zoneChangeObservable).thenReturn(
            Observable.fromIterable(supervisedInfos)
        )

        viewModel.initTransitionDetection()

        viewModel.settingsRelay.accept(settings)

        verify(soundInteractor, times(supervisedInfos.size)).playTransitionSound()
    }

    @Test
    fun `startCoachPlus invokes startToothBrushVibration when connection not null`() {
        viewModel = createViewModel("hello")
        val connection = mock<KLTBConnection>()
        whenever(gameInteractor.connection).thenReturn(connection)

        doNothing().whenever(viewModel).startToothbrushVibration()

        viewModel.startCoachPlus()

        verify(viewModel).startToothbrushVibration()
    }

    @Test
    fun `startCoachPlus pushes SomethingWrong action when connection null and not manual don't update viewState`() {
        viewModel = createViewModel("hello")

        val testObserver = viewModel.actionsObservable.test()

        viewModel.startCoachPlus()

        testObserver.assertValue { action ->
            action is CoachPlusActions.SomethingWrong && action.error is IllegalStateException
        }

        verify(viewModel, never()).updateViewState(any())
    }

    @Test
    fun `startCoachPlus updates view state to isPlaying true and isInit true`() {
        val testObserver = viewModel.viewStateFlowable.test()
        viewModel.startCoachPlus()

        testObserver.awaitCount(2).assertValueAt(1) { vs ->
            vs.isPlaying && vs.isInit
        }
    }

    @Test
    fun `resumeCoachPlus invokes startCoachPlus`() {
        doNothing().whenever(viewModel).startCoachPlus()

        viewModel.resumeCoachPlus()

        verify(viewModel).startCoachPlus()
    }

    @Test
    fun `stopCoachPlus invokes stopToothbrushVibration and controller onPause and update viewState`() {
        doNothing().whenever(viewModel).stopToothbrushVibration()
        doNothing().whenever(coachPlusController).onPause()

        val testObserver = viewModel.viewStateFlowable.test()

        viewModel.stopCoachPlus()

        // Same value as the init one because there is a distinctUntilChange on the viewStateFlowable
        testObserver.assertValue { vs ->
            !vs.isPlaying && vs.feedBackMessage == FeedBackMessage.EmptyFeedback
        }

        verify(coachPlusController).onPause()
        verify(viewModel).stopToothbrushVibration()
        verify(viewModel).updateViewState(any())
    }

    @Test
    fun `stopCoachPlus permanently parameter is preserved in state`() {
        doNothing().whenever(viewModel).stopToothbrushVibration()
        doNothing().whenever(coachPlusController).onPause()

        val testObserver = viewModel.viewStateFlowable.test()

        viewModel.stopCoachPlus(permanently = false)
        viewModel.stopCoachPlus(permanently = true)
        viewModel.stopCoachPlus(permanently = false)

        testObserver.assertValueCount(2) // last one is skipped due to distinctUntilChanged
        testObserver.assertValueAt(0) { vs -> !vs.isEnd }
        testObserver.assertValueAt(1) { vs -> vs.isEnd }
    }

    @Test
    fun `stopToothbrushVibration invokes vibrator off if connection not null and vibrator isOn true`() {
        val connection = mock<KLTBConnection>()
        val vibrator = mock<Vibrator>()

        whenever(gameInteractor.connection).thenReturn(connection)
        whenever(connection.vibrator()).thenReturn(vibrator)
        whenever(vibrator.isOn).thenReturn(true)
        whenever(vibrator.off()).thenReturn(Completable.complete())
        viewModel.stopToothbrushVibration()

        verify(vibrator).isOn
        verify(vibrator).off()
    }

    @Test
    fun `startToothbrushVibration invokes vibrator on if connection not null and vibrator isOn false`() {
        val connection = mock<KLTBConnection>()
        val vibrator = mock<Vibrator>()

        whenever(gameInteractor.connection).thenReturn(connection)
        whenever(connection.vibrator()).thenReturn(vibrator)
        whenever(vibrator.isOn).thenReturn(false)
        whenever(vibrator.on()).thenReturn(Completable.complete())
        viewModel.startToothbrushVibration()

        verify(vibrator).isOn
        verify(vibrator).on()
    }

    @Test
    fun `onTick invokes controller onTick and if sequenceFinish true invokes onGameFinish otherwise onGameRunning`() {
        val resultFinished = mock<CoachPlusControllerResult>()
        val resultNotFinished = mock<CoachPlusControllerResult>()

        whenever(resultFinished.sequenceFinished).thenReturn(true)
        whenever(resultNotFinished.sequenceFinished).thenReturn(false)
        whenever(coachPlusController.onTick()).thenReturn(resultNotFinished, resultFinished)

        doNothing().whenever(viewModel).onGameFinished(eq(true))
        doNothing().whenever(viewModel).onGameRunning(eq(resultNotFinished))

        viewModel.onTick(1)

        verify(coachPlusController).onTick()
        verify(viewModel).onGameRunning(eq(resultNotFinished))
        verify(viewModel, never()).onGameFinished(any())

        viewModel.onTick(2)

        verify(coachPlusController, times(2)).onTick()
        verify(viewModel).onGameRunning(eq(resultNotFinished))
        verify(viewModel).onGameFinished(any())
    }

    @Test
    fun `onGameRunning updateViewState with the value from result`() {
        val result = mock<CoachPlusControllerResult>()
        val expectedZone = MouthZone16.LoIncInt
        val expectedCompletionPercentage = 10
        val isBrushingGoodZone = true
        val expectedFeedback = FeedBackMessage.WrongIncisorsIntAngleFeedback

        whenever(result.zoneToBrush).thenReturn(expectedZone)
        whenever(result.completionPercent).thenReturn(expectedCompletionPercentage)
        whenever(result.brushingGoodZone).thenReturn(isBrushingGoodZone)
        whenever(result.feedBackMessage).thenReturn(expectedFeedback)

        viewModel.onGameRunning(result)

        viewModel.viewStateFlowable.test().awaitCount(2).assertValue { vs ->
            vs.isBrushingGoodZone == isBrushingGoodZone &&
                vs.currentZone == expectedZone &&
                vs.currentZoneProgress == expectedCompletionPercentage &&
                vs.feedBackMessage == expectedFeedback
        }

        verify(viewModel).updateViewState(any())
    }

    @Test
    fun `onGameFinishing invokes onGameFinished with the return of controller computeBrushingDuration greater or equals to MIN_BRUSHING_DURATION`() {
        doNothing().whenever(viewModel).onGameFinished(any())

        whenever(coachPlusController.computeBrushingDuration())
            .thenReturn(
                (MIN_BRUSHING_DURATION_SECONDS - 1).toInt(),
                (MIN_BRUSHING_DURATION_SECONDS + 1).toInt()
            )

        viewModel.onGameFinishing()

        verify(viewModel).onGameFinished(eq(false))
        verify(viewModel, never()).onGameFinished(eq(true))

        viewModel.onGameFinishing()

        verify(viewModel).onGameFinished(eq(false))
        verify(viewModel).onGameFinished(eq(true))
    }

    // TODO continue at onGameFinished

    @Test
    fun `onGameFinished invokes stopCoachPlus and uses KML to publish AVRO`() {
        viewModel = createViewModel("hello")

        doNothing().whenever(viewModel).stopCoachPlus(anyBoolean())
        doNothing().whenever(viewModel).onBrushingCompleted()

        val avroSubject = CompletableSubject.create()
        doReturn(avroSubject).whenever(viewModel).publishKmlAvroDataCompletable()

        viewModel.onGameFinished(true)

        verify(viewModel).stopCoachPlus(true)

        verify(coachPlusController, never()).getAvroTransitionsTable()
        verify(viewModel).onBrushingCompleted()

        assertTrue(avroSubject.hasObservers())
    }

    @Test
    fun `onGameFinished invokes stopCoachPlus and uses KML to publish AVRO and push Cancel action if finishedWithSuccess false`() {
        viewModel = createViewModel("hello")

        doNothing().whenever(viewModel).stopCoachPlus(anyBoolean())
        doNothing().whenever(viewModel).onBrushingCompleted()

        val avroSubject = CompletableSubject.create()
        doReturn(avroSubject).whenever(viewModel).publishKmlAvroDataCompletable()

        val testObserver = viewModel.actionsObservable.test()

        viewModel.onGameFinished(false)

        verify(viewModel).stopCoachPlus(true)

        verify(coachPlusController, never()).getAvroTransitionsTable()
        verify(viewModel, never()).onBrushingCompleted()

        assertTrue(avroSubject.hasObservers())

        testObserver.assertValue(CoachPlusActions.Cancel)
    }

    @Test
    fun `onGameFinished invokes stopCoachPlus and onBrushingCompleted if finishedWithSuccess true`() {

        doNothing().whenever(viewModel).stopCoachPlus(anyBoolean())
        doNothing().whenever(viewModel).onBrushingCompleted()

        viewModel.onGameFinished(true)

        verify(viewModel).stopCoachPlus(true)
        verify(coachPlusController, never()).getAvroTransitionsTable()
        verify(viewModel).onBrushingCompleted()
    }

    @Test
    fun `onGameFinished invokes stopCoachPlus and push Cancel action if finishedWithSuccess false`() {

        doNothing().whenever(viewModel).stopCoachPlus(anyBoolean())
        doNothing().whenever(viewModel).onBrushingCompleted()

        val testObserver = viewModel.actionsObservable.test()

        viewModel.onGameFinished(false)

        verify(viewModel).stopCoachPlus(true)
        verify(viewModel, never()).onBrushingCompleted()

        testObserver.assertValue(CoachPlusActions.Cancel)
    }

    @Test
    fun `initAmbientSound invokes pauseAmbientSound when isPlaying false and settings are available`() {
        viewModel = createViewModel(viewState = CoachPlusViewState(false, isPlaying = false))

        val settings = mock<CoachSettings>()

        doNothing().whenever(soundInteractor).pauseAmbientSound()

        viewModel.settingsRelay.accept(settings)

        viewModel.initAmbientSound()

        verify(soundInteractor).pauseAmbientSound()
        verify(soundInteractor, never()).playAmbientSound()
        verify(soundInteractor, never()).prepare(any())
    }

    @Test
    fun `initAmbientSound invokes prepare and playAmbientSound when isPlaying true and settings enableMusic true`() {
        viewModel = createViewModel(viewState = CoachPlusViewState(false, isPlaying = true))
        val settings = mock<CoachSettings>()
        val expectedUri = mock<Uri>()

        doNothing().whenever(soundInteractor).pauseAmbientSound()
        doNothing().whenever(soundInteractor).playAmbientSound()

        whenever(settings.enableMusic).thenReturn(true)
        whenever(settings.getUriOfMusic()).thenReturn(expectedUri)
        viewModel.settingsRelay.accept(settings)

        viewModel.initAmbientSound()

        verify(soundInteractor, never()).pauseAmbientSound()
        verify(soundInteractor).playAmbientSound()
        verify(soundInteractor).prepare(eq(expectedUri))
        verify(settings).getUriOfMusic()
    }

    @Test
    fun `initAmbientSound invokes nothings when isPlaying true and settings enableMusic false`() {
        whenever(coachSettingsRepository.getSettingsByProfileId(any())).thenReturn(Flowable.empty())
        viewModel =
            createViewModel("hello", viewState = CoachPlusViewState(false, isPlaying = true))

        val settings = mock<CoachSettings>()

        whenever(settings.enableMusic).thenReturn(false)
        viewModel.settingsRelay.accept(settings)

        viewModel.initAmbientSound()

        verify(settings, never()).getUriOfMusic()
        verify(soundInteractor, never()).pauseAmbientSound()
        verify(soundInteractor, never()).playAmbientSound()
        verify(soundInteractor, never()).prepare(any())
    }

    /*
    sendSupervisionInfo
     */

    @Test
    fun `calling sendSupervisionInfo multiple times disposes previous subscription`() {
        val connection = mock<KLTBConnection>()
        viewModel.sendSupervisionInfo(connection)
        val diposable = viewModel.supervisionDisposable

        viewModel.sendSupervisionInfo(connection)
        assertTrue(diposable!!.isDisposed)
    }

    @Test
    fun `sendSupervisionInfo invokes connection setSupervisedMouthZone as many times zoneChangeObservable emits`() {
        val connection = mock<KLTBConnection>()
        val toothbrush = mock<Toothbrush>()
        val supervisionInfos = listOf(
            SupervisionInfo(MouthZone16.LoIncInt, 1),
            SupervisionInfo(MouthZone16.LoMolLeExt, 2)
        )
        whenever(coachPlusController.zoneChangeObservable).thenReturn(
            Observable.fromIterable(
                supervisionInfos
            )
        )

        whenever(connection.toothbrush()).thenReturn(toothbrush)
        whenever(toothbrush.setSupervisedMouthZone(any(), any())).thenReturn(Single.never())

        viewModel.sendSupervisionInfo(connection)

        verify(toothbrush, times(supervisionInfos.size)).setSupervisedMouthZone(any(), any())
    }

    @Test
    fun `sendSupervisionInfo do nothing when toggle CoachPlusPlaqlessSupervisionFeature is false`() {
        plaqlesssSupervisionFeature.value = false

        verify(coachPlusController, never()).zoneChangeObservable
    }

    @Test
    fun `calling listenToRingLEDChange multiple times disposes previous subscription`() {
        val connection = mock<KLTBConnection>()
        viewModel.listenToRingLedChange(connection)
        val diposable = viewModel.ringLedColorUseCaseDisposable

        viewModel.listenToRingLedChange(connection)
        assertTrue(diposable!!.isDisposed)
    }

    @Test
    fun `listenToRingLedChange invokes onRingLedColorChanged as many times getRingLedColor emits`() {
        val connection = mock<KLTBConnection>()

        val colors = listOf(1, 2, 3)
        whenever(ringLedColorUseCase.getRingLedColor(eq(connection))).thenReturn(
            Flowable.fromIterable(
                colors
            )
        )

        doNothing().whenever(viewModel).onRingLedColorChanged(any())

        viewModel.listenToRingLedChange(connection)

        verify(viewModel, times(colors.size)).onRingLedColorChanged(any())
    }

    @Test
    fun `onRingLedColorChanged updates viewState with color`() {
        val expectedColor = Color.GRAY

        viewModel.onRingLedColorChanged(expectedColor)

        viewModel.viewStateFlowable.test().assertValue { vs ->
            vs.ringLedColor == expectedColor
        }
    }

    @Test
    fun `startCoachPlus does not start toothbrush vibration in manual mode`() {
        viewModel = createViewModel(null) // is manual Mode
        whenever(gameInteractor.connection).thenReturn(mock())
        doNothing().whenever(viewModel).startToothbrushVibration()

        viewModel.startCoachPlus()
        verify(viewModel, never()).startToothbrushVibration()
    }

    /*
    nullifyCurrentZone
     */

    @Test
    fun `nullifyCurrentZone updates the view state with null current zone`() {
        val testObserver = viewModel.currentZone.test()

        viewModel.nullifyCurrentZone()

        assertNull(testObserver.value())
    }

    /*
    publishAvro
     */

    @Test
    fun `publishAvro invokes publishKmlAvroDataCompletable`() {
        val avroTransition = IntArray(0)
        whenever(coachPlusController.getAvroTransitionsTable()).thenReturn(avroTransition)
        doReturn(Completable.complete()).whenever(viewModel).publishKmlAvroDataCompletable()

        viewModel.publishAvro()

        verify(viewModel).publishKmlAvroDataCompletable()
    }

    /*
    createAvroBrushingSessionSingle
     */

    @Test
    fun `createAvroBrushingSessionSingle emits error when connection is null`() {
        whenever(gameInteractor.connection).thenReturn(null)

        viewModel.createAvroBrushingSessionSingle()
            .test()
            .assertNotComplete()
            .assertNoValues()
            .assertError(IllegalStateException::class.java)
    }

    @Test
    fun `createAvroBrushingSessionSingle emits kmlAvroCreator's createBrushingSession's result`() {
        val expectedConnection = KLTBConnectionBuilder.createAndroidLess().build()
        whenever(gameInteractor.connection).thenReturn(expectedConnection)

        val expectedTransitions = IntArray(0)
        whenever(coachPlusController.getAvroTransitionsTable()).thenReturn(expectedTransitions)

        val expectedBrushingSession = mock<BrushingSession>()
        whenever(
            kmlAvroCreator.createBrushingSession(
                eq(expectedConnection),
                eq(COACH_PLUS),
                eq(expectedTransitions),
                any(),
                eq(true)
            )
        ).thenReturn(Single.just(expectedBrushingSession))

        viewModel.createAvroBrushingSessionSingle()
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValue(expectedBrushingSession)

        verify(gameInteractor).connection
        verify(kmlAvroCreator)
            .createBrushingSession(
                eq(expectedConnection),
                eq(COACH_PLUS),
                eq(expectedTransitions),
                any(),
                eq(true)
            )
    }

    /*
    publishKmlAvroDataCompletable
     */

    @Test
    fun `publishKmlAvroDataCompletable packs and submits KML AVRO data`() {
        val expectedBrushingSession = mock<BrushingSession>()
        doReturn(Single.just(expectedBrushingSession))
            .whenever(viewModel)
            .createAvroBrushingSessionSingle()

        val expectedAvroData = mock<CharVector>()
        doReturn(Single.just(expectedAvroData))
            .whenever(viewModel)
            .generateAvroDataSingle(expectedBrushingSession)

        whenever(kmlAvroCreator.submitAvroData(expectedAvroData))
            .thenReturn(Completable.complete())

        viewModel.publishKmlAvroDataCompletable()
            .test()
            .assertNoErrors()
            .assertComplete()

        verify(viewModel).createAvroBrushingSessionSingle()
        verify(viewModel).generateAvroDataSingle(expectedBrushingSession)
        verify(kmlAvroCreator).submitAvroData(expectedAvroData)
    }

    /*
    onBrushingCompleted
     */

    @Test
    fun `onBrushingCompleted invokes brushingCreator with args`() {
        val connection = mock<KLTBConnection>()
        val createBrushingData = mock<CreateBrushingData>()
        viewModel = createViewModel(macAddress = "helloMac")
        doNothing().whenever(brushingCreator)
            .onBrushingCompleted(eq(true), eq(connection), eq(createBrushingData))

        whenever(gameInteractor.connection).thenReturn(connection)
        whenever(coachPlusController.createBrushingData()).thenReturn(createBrushingData)

        viewModel.onBrushingCompleted()

        verify(brushingCreator).onBrushingCompleted(
            eq(false),
            eq(connection),
            eq(createBrushingData)
        )
    }

    @Test
    fun `onBrushingCompleted updates zoneProgressData to finished one`() {
        viewModel.updateViewState { copy(zoneProgressData = zoneProgressData.updateProgressOnZone(0, 0.5f)) }
        val zoneProgressData = viewModel.getViewState()!!.zoneProgressData
        assertTrue(zoneProgressData.zones[0].isOngoing)

        viewModel.onBrushingCompleted()

        val updatedZoneProgressData = viewModel.getViewState()!!.zoneProgressData
        val updatedZones = zoneProgressData.zones.size
        for (zone in 0 until updatedZones) {
            assertFalse(updatedZoneProgressData.zones[zone].isOngoing)
        }
    }

    @Test
    fun `onBrushingCompleted create a brushing with null as connection when is manual even gameInterator have one`() {
        whenever(gameInteractor.connection).then(mock())

        viewModel.onBrushingCompleted()

        verify(brushingCreator).onBrushingCompleted(eq(true), eq(null), anyOrNull())
    }

    // Analytics

    @Test
    fun `send pause to analytics only if game was on`() {
        viewModel.startCoachPlus()
        verify(eventTracker, never()).sendEvent(any())

        viewModel.pauseCoachPlus()
        verify(eventTracker, times(1)).sendEvent(NoOpCoachPlusAnalytics.pause())

        viewModel.pauseCoachPlus()
        verify(eventTracker, times(1)).sendEvent(NoOpCoachPlusAnalytics.pause())
    }

    @Test
    fun `send resume to analytics if user clicked resume button`() {
        viewModel.startCoachPlus()
        verify(eventTracker, never()).sendEvent(any())

        viewModel.pauseCoachPlus()
        verify(eventTracker, times(1)).sendEvent(NoOpCoachPlusAnalytics.pause())

        viewModel.onResumeButtonClick()
        verify(eventTracker, times(1)).sendEvent(NoOpCoachPlusAnalytics.resume())
    }

    @Test
    fun `send resume to analytics if user started the brush while game was off`() {
        viewModel.startCoachPlus()
        verify(eventTracker, never()).sendEvent(any())

        viewModel.pauseCoachPlus()
        verify(eventTracker, times(1)).sendEvent(NoOpCoachPlusAnalytics.pause())

        viewModel.onVibratorOn(
            KLTBConnectionBuilder.createAndroidLess().withVibration(true).build())
        verify(eventTracker, times(1)).sendEvent(NoOpCoachPlusAnalytics.resume())
    }

    @Test
    fun `send restart to analytics if user clicked restart button`() {
        viewModel.startCoachPlus()
        verify(eventTracker, never()).sendEvent(any())

        viewModel.pauseCoachPlus()
        verify(eventTracker, times(1)).sendEvent(NoOpCoachPlusAnalytics.pause())

        viewModel.onRestartButtonClick()
        verify(eventTracker, times(1)).sendEvent(NoOpCoachPlusAnalytics.restart())
    }

    @Test
    fun `send quit to analytics if user clicked quit button`() {
        viewModel.actionsObservable.subscribe()

        viewModel.startCoachPlus()
        verify(eventTracker, never()).sendEvent(any())

        viewModel.pauseCoachPlus()
        verify(eventTracker, times(1)).sendEvent(NoOpCoachPlusAnalytics.pause())

        viewModel.onQuitButtonClick()
        verify(eventTracker, times(1)).sendEvent(NoOpCoachPlusAnalytics.quit())
    }

    /*
    Utils
     */

    private fun createViewModel(
        macAddress: String? = null,
        model: ToothbrushModel? = null,
        colorSet: CoachPlusColorSet = this.colorSet,
        viewState: CoachPlusViewState? = null
    ) = spy(
        CoachPlusViewModel(
            viewState,
            Optional.fromNullable(macAddress),
            model,
            gameInteractor,
            gameToothbrushInteractorFacade,
            lostConnectionHandler,
            soundInteractor,
            colorSet,
            connector,
            coachPlusController,
            coachSettingsRepository,
            zoneHintProvider,
            Duration.ofMillis(25),
            confirmBrushingModeUseCase,
            ringLedColorUseCase,
            brushingCreator,
            kmlAvroCreator,
            keepScreenOnController,
            colorProvider,
            featuresToggle,
            NoOpCoachPlusAnalytics
        )
    )
}

private object NoOpCoachPlusAnalytics : CoachPlusAnalytics {

    override fun main() = AnalyticsEvent("main")

    override fun pause() = AnalyticsEvent("pause")

    override fun quit() = AnalyticsEvent("quit")

    override fun resume() = AnalyticsEvent("resume")

    override fun restart() = AnalyticsEvent("restart")
}

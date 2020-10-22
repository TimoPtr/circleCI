package com.kolibree.android.sba.testbrushing.duringsession

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.commons.ToothbrushModel.CONNECT_M1
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.mouthmap.widget.timer.MouthMapTimer
import com.kolibree.android.processedbrushings.CheckupData
import com.kolibree.android.processedbrushings.exception.ProcessedBrushingNotAvailableException
import com.kolibree.android.sba.testbrushing.TestBrushingNavigator
import com.kolibree.android.sba.testbrushing.base.HideFinishBrushingDialog
import com.kolibree.android.sba.testbrushing.base.LostConnectionStateChanged
import com.kolibree.android.sba.testbrushing.base.NoneAction
import com.kolibree.android.sba.testbrushing.base.ShowFinishBrushingDialog
import com.kolibree.android.sba.testbrushing.brushing.TestBrushingResultsProvider
import com.kolibree.android.sba.testbrushing.brushing.creator.TestBrushingCreator
import com.kolibree.android.sba.testbrushing.duringsession.SessionStep.ANALYZING_STEP
import com.kolibree.android.sba.testbrushing.duringsession.SessionStep.FINISH_STEP
import com.kolibree.android.sba.testbrushing.duringsession.SessionStep.START_STEP
import com.kolibree.android.sba.testbrushing.duringsession.SessionStep.TOOTHBRUSH_STEP
import com.kolibree.android.sba.testbrushing.duringsession.timer.CarouselTimer
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.vibrator.Vibrator
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.sdk.disconnection.LostConnectionHandler
import com.kolibree.android.sdk.disconnection.LostConnectionHandler.State
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.utils.ReflectionUtils
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import org.junit.Assert
import org.junit.Test

class TestBrushingDuringSessionViewModelTest : BaseUnitTest() {

    private val serviceProvider = mock<ServiceProvider>()
    private val navigator = mock<TestBrushingNavigator>()
    private val carouselTimer = mock<CarouselTimer>()
    private val brushingCreator = mock<TestBrushingCreator>()
    private val resultProvider = mock<TestBrushingResultsProvider>()
    private val timer = mock<MouthMapTimer>()
    private val handler = mock<LostConnectionHandler>()

    internal lateinit var viewModel: TestBrushingDuringSessionViewModel

    override fun setup() {
        super.setup()
        whenever(carouselTimer.observable()).thenReturn(Observable.just(42))
        viewModel = createSpyViewModel()
    }

    private fun createSpyViewModel(model: ToothbrushModel = MODEL) = spy(
        TestBrushingDuringSessionViewModel(
            serviceProvider = serviceProvider,
            navigator = navigator,
            carouselTimer = carouselTimer,
            toothbrushMac = MAC,
            toothbrushModel = model,
            brushingCreator = brushingCreator,
            brushingResultsProvider = resultProvider,
            mouthMapTimer = timer,
            lostConnectionHandler = handler
        )
    )

    /*
    ON CLEARED
     */

    @Test
    fun `onCleared does not crash if connection is null`() {
        ReflectionUtils.invokeProtectedVoidMethod(viewModel, "onCleared")
    }

    @Test
    fun `onCleared disables detection notifications`() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withVibration(true)
            .withSupportVibrationCommands()
            .build()
        viewModel.currentConnection = connection

        ReflectionUtils.invokeProtectedVoidMethod(viewModel, "onCleared")

        verify(connection.detectors()).disableDetectionNotifications()
    }

    @Test
    fun `onCleared invokes vibratorOff if vibration is on`() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withVibration(true)
            .withSupportVibrationCommands()
            .build()
        viewModel.currentConnection = connection

        ReflectionUtils.invokeProtectedVoidMethod(viewModel, "onCleared")

        verify(connection.vibrator()).off()
    }

    @Test
    fun `onCleared does not invoke vibratorOff if vibration is off`() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withVibration(false)
            .withSupportVibrationCommands()
            .build()
        viewModel.currentConnection = connection

        ReflectionUtils.invokeProtectedVoidMethod(viewModel, "onCleared")

        verify(connection.vibrator(), never()).off()
    }

    @Test
    fun `onUserDismissedLostConnectionDialog invokes navigator finishScreen`() {
        viewModel.onUserDismissedLostConnectionDialog()

        verify(navigator).finishScreen()
    }

    @Test
    fun onVibratorStateChanged_isVibratorOn_emitsHideFinishBrushingDialog() {
        viewModel.onVibratorStateChanged(true)

        val expectedViewState = viewModel.viewState.copy(action = HideFinishBrushingDialog)
        verify(viewModel).emitState(expectedViewState)
    }

    @Test
    fun onVibratorStateChanged_isVibratorOff_emitShowFinishBrushingDialog() {
        viewModel.onVibratorStateChanged(false)

        val expectedViewState = viewModel.viewState.copy(action = ShowFinishBrushingDialog)
        verify(viewModel).emitState(expectedViewState)
    }

    @Test
    fun onVibratorStateChanged_isVibratorOn_invokesTimerResume() {
        viewModel.onVibratorStateChanged(true)

        verify(carouselTimer).resume()
    }

    @Test
    fun `onVibratorStateChanged with vibrator on invokes resume method`() {
        viewModel.onVibratorStateChanged(true)

        verify(timer).resume()
    }

    @Test
    fun onVibratorStateChanged_isVibratorOn_invokesBrushingCreatorResume() {
        val connection = mock<KLTBConnection>()
        viewModel.currentConnection = connection

        viewModel.onVibratorStateChanged(true)

        verify(brushingCreator).resume(connection)
    }

    @Test
    fun onVibratorStateChanged_isVibratorOff_invokesTimerPause() {
        viewModel.onVibratorStateChanged(false)

        verify(carouselTimer).pause()
    }

    @Test
    fun `onVibratorStateChanged with vibrator off invokes pause method`() {
        viewModel.onVibratorStateChanged(false)

        verify(timer).pause()
    }

    @Test
    fun onVibratorStateChanged_isVibratorOff_invokesBrushingCreatorPause() {
        val connection = mock<KLTBConnection>()
        viewModel.currentConnection = connection

        viewModel.onVibratorStateChanged(false)

        verify(brushingCreator).pause(connection)
    }

    @Test
    fun nextItem_updateCurrentStep() {
        viewModel.currentStep = START_STEP

        viewModel.nextItem(0)
        Assert.assertEquals(ANALYZING_STEP, viewModel.currentStep)

        viewModel.nextItem(0)
        Assert.assertEquals(TOOTHBRUSH_STEP, viewModel.currentStep)

        viewModel.nextItem(0)
        Assert.assertEquals(FINISH_STEP, viewModel.currentStep)

        viewModel.nextItem(0)
        Assert.assertEquals(START_STEP, viewModel.currentStep)
    }

    @Test
    fun onResume_invokesTimerResume() {
        doNothing().whenever(carouselTimer).resume()

        viewModel.onResume(mock())

        verify(carouselTimer).resume()
    }

    @Test
    fun `onResume invokes timer resume method`() {
        doNothing().whenever(timer).resume()

        viewModel.onResume(mock())

        verify(timer).resume()
    }

    @Test
    fun onPause_invokesTimerPause() {
        doNothing().whenever(carouselTimer).pause()

        viewModel.onPause(mock())

        verify(carouselTimer).pause()
    }

    @Test
    fun `onPause invokes timer pause`() {
        doNothing().whenever(timer).pause()

        viewModel.onPause(mock())

        verify(timer).pause()
    }

    @Test
    fun resetActionViewState_setNoneAction() {
        val currentViewState = viewModel.viewState.copy(action = HideFinishBrushingDialog)
        val expectedViewState = currentViewState.copy(action = NoneAction)

        viewModel.resetActionViewState()

        Assert.assertEquals(expectedViewState, viewModel.viewState)
    }

    @Test
    fun userFinishedBrushing_invokesNavigateToOptimizeAnalysisScreen() {
        viewModel.userFinishedBrushing()

        verify(navigator).navigateToOptimizeAnalysisScreen()
    }

    @Test
    fun userFinishedBrushing_invokesBrushingCreatorCreate() {
        val connection = mock<KLTBConnection>()
        viewModel.currentConnection = connection

        viewModel.userFinishedBrushing()

        verify(brushingCreator).create(connection)
    }

    @Test
    fun userFinishedBrushing_invokesBrushingResultsProviderInit() {
        val connection = mock<KLTBConnection>()
        viewModel.currentConnection = connection
        val checkupData = mock<CheckupData>()
        whenever(brushingCreator.create(connection)).thenReturn(checkupData)
        doNothing().whenever(resultProvider).init(checkupData)

        viewModel.userFinishedBrushing()

        verify(resultProvider).init(checkupData)
    }

    @Test
    fun userResumedBrushing_manualToothbrush_doNothing() {
        viewModel = createSpyViewModel(CONNECT_M1)
        val vibrator = mock<Vibrator>()
        val connection = mock<KLTBConnection>()
        whenever(connection.vibrator()).thenReturn(vibrator)
        whenever(vibrator.on()).thenReturn(Completable.complete())
        viewModel.currentConnection = connection

        viewModel.userResumedBrushing()
        verify(vibrator, times(0)).on()
        verify(viewModel, times(0)).turnVibratorOnSuccess()
    }

    @Test
    fun userFinishedBrushing_callsFinishScreenOnProcessedBrushingNotAvailableException() {
        val connection = mock<KLTBConnection>()
        viewModel.currentConnection = connection
        whenever(brushingCreator.create(connection))
            .thenThrow(ProcessedBrushingNotAvailableException())

        viewModel.userFinishedBrushing()

        verify(navigator, never()).navigateToOptimizeAnalysisScreen()
        verify(navigator).finishScreen()
    }

    @Test
    fun userResumedBrushing_electricToothbrush_invokesVibratorOn() {
        FailEarly.overrideDelegateWith(NoopTestDelegate)

        val vibrator = mock<Vibrator>()
        val connection = mock<KLTBConnection>()
        whenever(connection.vibrator()).thenReturn(vibrator)
        whenever(vibrator.on()).thenReturn(Completable.complete())
        viewModel.currentConnection = connection

        viewModel.userResumedBrushing()
        verify(vibrator).on()
        verify(viewModel).turnVibratorOnSuccess()
    }

    @Test
    fun turnVibratorOnSuccess_invokesTimerResume() {
        viewModel.turnVibratorOnSuccess()

        verify(carouselTimer).resume()
    }

    @Test
    fun `turnVibratorOnSuccess invokes timer Resume`() {
        viewModel.turnVibratorOnSuccess()

        verify(timer).resume()
    }

    @Test
    fun turnVibratorOnSuccess_invokesBrushingCreatorResume() {
        val connection = mock<KLTBConnection>()
        viewModel.currentConnection = connection

        viewModel.turnVibratorOnSuccess()

        verify(brushingCreator).resume(connection)
    }

    @Test
    fun `connectionChanged for state ConnectionLost invokes pauseTimers()`() {
        viewModel.connectionChanged(State.CONNECTION_LOST)

        verify(viewModel).pauseTimers()
    }

    @Test
    fun `connectionChanged for state Connecting invokes pauseTimers()`() {
        viewModel.connectionChanged(State.CONNECTING)

        verify(viewModel).pauseTimers()
    }

    @Test
    fun `connectionChanged for state Active invokes resumeTimers()`() {
        viewModel.connectionChanged(State.CONNECTION_ACTIVE)

        verify(viewModel).resumeTimers()
    }

    @Test
    fun `connectionChanged for state Active invokes brushingCreator notifyReconnection()`() {
        viewModel.connectionChanged(State.CONNECTION_ACTIVE)

        verify(brushingCreator).notifyReconnection()
    }

    @Test
    fun `connectionChanged invokes emitState`() {
        val state = State.CONNECTION_LOST

        val viewState = viewModel.viewState
        viewModel.connectionChanged(state)

        val viewStateExpected = viewState.copy(action = LostConnectionStateChanged(state))
        verify(viewModel).emitState(viewStateExpected)
    }

    companion object {
        private const val MAC = "CA:FE:BA:BE:00:11"
        private val MODEL = ToothbrushModel.CONNECT_E2
    }
}

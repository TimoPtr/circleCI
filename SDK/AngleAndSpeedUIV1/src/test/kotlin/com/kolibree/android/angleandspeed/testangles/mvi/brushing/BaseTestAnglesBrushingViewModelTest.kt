/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.testangles.mvi.brushing

import com.kolibree.android.angleandspeed.common.logic.AngleAndSpeedUseCase
import com.kolibree.android.angleandspeed.common.logic.model.AngleAndSpeedFeedback
import com.kolibree.android.angleandspeed.common.logic.model.AngleFeedback
import com.kolibree.android.app.interactor.GameInteractor
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.game.KeepScreenOnController
import com.kolibree.android.game.sensors.interactors.GameToothbrushInteractorFacade
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.disconnection.LostConnectionHandler
import com.kolibree.android.test.LifecycleObserverTester
import com.kolibree.android.test.lifecycleTester
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.mockFacadeWithLifecycleSupport
import com.kolibree.kml.MouthZone16
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.processors.PublishProcessor
import io.reactivex.subjects.PublishSubject

internal abstract class BaseTestAnglesBrushingViewModelTest<VM : TestAnglesBrushingViewModel> :
    BaseUnitTest() {

    internal lateinit var viewModel: VM

    protected lateinit var viewModelLifecycleTester: LifecycleObserverTester

    protected lateinit var connection: KLTBConnection

    protected val gameInteractor: GameInteractor = mock()

    protected val facade: GameToothbrushInteractorFacade = mockFacadeWithLifecycleSupport()

    protected val angleAndSpeedUseCase: AngleAndSpeedUseCase = mock()

    protected val angleAndSpeedTestProcessor = PublishProcessor.create<AngleAndSpeedFeedback>()

    protected val lostConnectionHandler: LostConnectionHandler = mock()

    protected val lostConnectionTestProcessor = PublishSubject.create<LostConnectionHandler.State>()

    protected val keepScreenOnController: KeepScreenOnController = mock()

    companion object {

        internal const val MAC_ADDRESS = "00:00:00:00:00:00"

        internal val PRESCRIBED_ZONES = emptyArray<MouthZone16>()
    }

    override fun setup() {
        super.setup()

        connection = KLTBConnectionBuilder.createAndroidLess()
            .withMac(MAC_ADDRESS)
            .withVibration(true)
            .build()
        whenever(gameInteractor.connection).then { connection }

        whenever(angleAndSpeedUseCase.angleAndSpeedFlowable).thenReturn(angleAndSpeedTestProcessor)
        whenever(lostConnectionHandler.connectionObservable(MAC_ADDRESS))
            .thenReturn(lostConnectionTestProcessor)

        viewModel = spy(createViewModel())

        viewModelLifecycleTester = viewModel.lifecycleTester()
    }

    internal abstract fun createViewModel(): VM

    protected fun updateStateWithAngles(angleFeedback: AngleFeedback) {
        viewModel.updateViewState {
            copy(angleDegrees = angleFeedback)
        }
    }

    protected fun updateStateWithZoneCorrect(isZoneCorrect: Boolean) {
        viewModel.updateViewState {
            copy(isZoneCorrect = isZoneCorrect)
        }
    }

    protected fun updateStateWith(
        isZoneCorrect: Boolean,
        vibrationOn: Boolean,
        lostConnectionState: LostConnectionHandler.State
    ) {
        viewModel.updateViewState {
            copy(
                isZoneCorrect = isZoneCorrect,
                vibrationOn = vibrationOn,
                lostConnectionState = lostConnectionState
            )
        }
    }

    protected fun updateStateWithAnglesAndZoneCorrect(
        angleFeedback: AngleFeedback,
        isZoneCorrect: Boolean
    ) {
        viewModel.updateViewState {
            copy(angleDegrees = angleFeedback, isZoneCorrect = isZoneCorrect)
        }
    }
}

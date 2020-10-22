/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.mvi.brushstart

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.app.interactor.GameInteractor
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush
import com.kolibree.android.sdk.connection.vibrator.Vibrator
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.test.lifecycleTester
import com.kolibree.android.test.pushLifecycleTo
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import org.junit.Test

class BaseBrushStartViewModelTest : BaseUnitTest() {

    private val state = BrushStartViewState(
        ToothbrushModel.CONNECT_E1,
        "com.kolibree.under.text",
        "00:00:00:00:00:00"
    )

    private val gameInteractor: GameInteractor = mock()

    private lateinit var viewModel: BaseBrushStartViewModel<*>

    override fun setup() {
        super.setup()
        viewModel = spy(BaseBrushStartViewModelUnderTest(state, gameInteractor))
    }

    @Test
    fun `onCreate sets lifecycle owner on gameInteractor`() {
        val lifecycleOwner: LifecycleOwner = mock()

        viewModel.lifecycleTester(lifecycleOwner = lifecycleOwner)
            .pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        verify(gameInteractor).setLifecycleOwner(lifecycleOwner)
    }

    @Test
    fun `listeners are active between start and stop`() {
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)
        verify(viewModel).registerListeners()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_STOP)
        verify(viewModel).unregisterListeners()
    }

    @Test
    fun `onCleared calls unregisterListeners`() {
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)
        viewModel.onCleared()

        verify(viewModel).unregisterListeners()
    }

    @Test
    fun `registerListeners adds VM as listener to gameInteractor`() {
        viewModel.registerListeners()

        verify(gameInteractor).addListener(viewModel)
    }

    @Test
    fun `unregisterListeners removes VM as listener from gameInteractor`() {
        viewModel.unregisterListeners()

        verify(gameInteractor).removeListener(viewModel)
    }

    @Test
    fun `onVibratorOn filters out connections for different model and MAC`() {
        viewModel.onVibratorOn(
            mockConnection(
                state.mac,
                ToothbrushModel.CONNECT_B1
            )
        )
        viewModel.onVibratorOn(
            mockConnection(
                "00:00:xx:xx:00:00",
                state.model
            )
        )

        verify(gameInteractor, never()).removeListener(viewModel)
        verify(viewModel, never()).onBrushStarted(any())
    }

    @Test
    fun `onVibratorOn passes matching connection through`() {
        val connection = mockConnection(
            state.mac,
            state.model
        )
        viewModel.onVibratorOn(connection)

        verify(gameInteractor).removeListener(viewModel)
        verify(viewModel).onBrushStarted(state)
    }

    @Test
    fun `onKolibreeServiceConnected turns off vibration if it was on`() {
        val service: KolibreeService = mock()
        val connection = mockConnection(state.mac, state.model, vibratorOn = true)
        doReturn(connection)
            .whenever(service).getConnection(state.mac)

        viewModel.onKolibreeServiceConnected(service)

        verify(connection.vibrator()).off()
    }

    companion object {

        private fun mockConnection(
            mac: String,
            model: ToothbrushModel,
            vibratorOn: Boolean = false
        ): KLTBConnection {
            val connection: KLTBConnection = mock()
            val toothbrush: Toothbrush = mock()
            val vibrator: Vibrator = mock()
            whenever(connection.vibrator()).thenReturn(vibrator)
            whenever(connection.toothbrush()).thenReturn(toothbrush)
            whenever(toothbrush.mac).thenReturn(mac)
            whenever(toothbrush.model).thenReturn(model)
            whenever(vibrator.isOn).thenReturn(vibratorOn)
            whenever(vibrator.off()).thenReturn(Completable.complete())
            return connection
        }
    }
}

private class BaseBrushStartViewModelUnderTest(
    viewState: BrushStartViewState,
    gameInteractor: GameInteractor
) : BaseBrushStartViewModel<NoActions>(viewState, gameInteractor) {

    override fun onBrushStarted(state: BrushStartViewState) {
        // no-op
    }
}

/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeCustomizer
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeTweaker
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.pairing.assistant.PairingAssistant
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import org.junit.Test

internal class TweakerViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: TweakerViewModel

    private val navigator: TweakerNavigator = mock()

    private val pairingAssistant: PairingAssistant = mock()

    private val connection = KLTBConnectionBuilder.createAndroidLess()
        .withBrushingMode()
        .build()

    override fun setup() {
        super.setup()

        val tweaker = mock<BrushingModeCustomizer>(
            extraInterfaces = arrayOf(BrushingModeTweaker::class)
        )
        whenever(connection.brushingMode().customize()).thenReturn(tweaker)

        viewModel = TweakerViewModel(
            initialViewState = TweakerViewState.initial(),
            navigator = navigator,
            connection = connection,
            pairingAssistant = pairingAssistant
        )
    }

    /*
    onDisconnectButtonClick
     */

    @Test
    fun `onDisconnectButtonClick disconnects current connection and navigates to pairing`() {
        whenever(pairingAssistant.unpair(any())).thenReturn(Completable.complete())

        viewModel.onDisconnectButtonClick()

        verify(pairingAssistant).unpair(eq(connection.toothbrush().mac))
        verify(navigator).navigateToPairingActivity()
    }
}

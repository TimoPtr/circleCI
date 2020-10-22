/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.usecases

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.pairing.PairingFlowSharedFacade
import com.kolibree.android.app.ui.pairing.usecases.ConnectionConfirmedNavigationAction.FINISH
import com.kolibree.android.app.ui.pairing.usecases.ConnectionConfirmedNavigationAction.MODEL_MISMATCH
import com.kolibree.android.app.ui.pairing.usecases.ConnectionConfirmedNavigationAction.NO_BLINKING_CONNECTION
import com.kolibree.android.app.ui.pairing.usecases.ConnectionConfirmedNavigationAction.SIGN_UP
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNull
import org.junit.Test

internal class NextNavigationActionUseCaseTest : BaseUnitTest() {
    private val sharedFacade: PairingFlowSharedFacade = mock()

    val humUseCase = NextNavigationActionUseCase(
        sharedFacade,
        ToothbrushModel.values().filter { it.isHumToothbrush }.toSet()
    )
    val colgateConnectUseCase = NextNavigationActionUseCase(
        sharedFacade,
        ToothbrushModel.values().filter { !it.isHumToothbrush }.toSet()
    )

    @Test
    fun `onConnectionConfirmed returns NO_BLINKING_CONNECTION when blinking connection is null`() {
        assertNull(sharedFacade.blinkingConnection())

        assertEquals(
            NO_BLINKING_CONNECTION,
            humUseCase.nextNavitationStep()
        )
    }

    @Test
    fun `onConnectionConfirmed returns SIGN_UP when blinking connection is not null, it's a hum toothbrush and it's onboarding flow`() {
        mockBlinkingConnection(isHumToothbrush = true)

        mockIsOnboardingFlow(isOnboardingFlow = true)

        assertEquals(
            SIGN_UP,
            humUseCase.nextNavitationStep()
        )
    }

    @Test
    fun `onConnectionConfirmed returns MODEL_MISTACH when blinking connection is not null, it's NOT a hum toothbrush on HUM App and it's onboarding flow`() {
        mockBlinkingConnection(isHumToothbrush = false)

        mockIsOnboardingFlow(isOnboardingFlow = true)

        assertEquals(
            MODEL_MISMATCH,
            humUseCase.nextNavitationStep()
        )
    }

    @Test
    fun `onConnectionConfirmed returns MODEL_MISTACH when blinking connection is not null, it's NOT a hum toothbrush on HUM App and it's NOT onboarding flow`() {
        mockBlinkingConnection(isHumToothbrush = false)

        mockIsOnboardingFlow(isOnboardingFlow = false)

        assertEquals(
            MODEL_MISMATCH,
            humUseCase.nextNavitationStep()
        )
    }

    @Test
    fun `onConnectionConfirmed returns MODEL_MISTACH when blinking connection is not null, it's a hum toothbrush on CC App and it's onboarding flow`() {
        mockBlinkingConnection(isHumToothbrush = true)

        mockIsOnboardingFlow(isOnboardingFlow = true)

        assertEquals(
            MODEL_MISMATCH,
            colgateConnectUseCase.nextNavitationStep()
        )
    }

    @Test
    fun `onConnectionConfirmed returns MODEL_MISTACH when blinking connection is not null, it's a hum toothbrush on CC App and it's NOT onboarding flow`() {
        mockBlinkingConnection(isHumToothbrush = true)

        mockIsOnboardingFlow(isOnboardingFlow = false)

        assertEquals(
            MODEL_MISMATCH,
            colgateConnectUseCase.nextNavitationStep()
        )
    }

    @Test
    fun `onConnectionConfirmed returns FINISH when blinking connection is not null, it's a hum toothbrush and it's NOT onboarding flow`() {
        mockBlinkingConnection(isHumToothbrush = true)

        mockIsOnboardingFlow(isOnboardingFlow = false)

        assertEquals(
            FINISH,
            humUseCase.nextNavitationStep()
        )
    }

    /*
    Utils
     */

    private fun mockBlinkingConnection(isHumToothbrush: Boolean) {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .apply {
                if (isHumToothbrush) {
                    withModel(ToothbrushModel.HUM_BATTERY)
                } else {
                    withModel(ToothbrushModel.CONNECT_E2)
                }
            }
            .build()

        whenever(sharedFacade.blinkingConnection()).thenReturn(connection)
    }

    private fun mockIsOnboardingFlow(isOnboardingFlow: Boolean) {
        whenever(sharedFacade.isOnboardingFlow()).thenReturn(isOnboardingFlow)
    }
}

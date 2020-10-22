/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.test.TestForcedException
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Test

internal class ConfirmBrushingModeUseCaseTest : BaseUnitTest() {
    private val brushingModeRepository: BrushingModeRepository = mock()
    private val toothbrushesUseCase: BrushingProgramToothbrushesUseCase = mock()

    private val confirmUserModeUseCase =
        ConfirmBrushingModeUseCaseImpl(brushingModeRepository, toothbrushesUseCase)

    @Test
    fun `confirmBrushingModeCompletable invokes setForProfile on brushingModeRepository`() {
        prepareToothbrushes()

        confirmUserModeUseCase.confirmBrushingModeCompletable(PROFILE_ID, defaultBrushingMode)
            .test()
            .assertComplete()

        verify(brushingModeRepository).setForProfile(PROFILE_ID, defaultBrushingMode)
    }

    @Test
    fun `confirmBrushingModeCompletable completes if no toothbrush associated`() {
        prepareToothbrushes()

        confirmUserModeUseCase.confirmBrushingModeCompletable(PROFILE_ID, defaultBrushingMode)
            .test()
            .assertComplete()
    }

    @Test
    fun `confirmBrushingModeCompletable only sets brushing mode on active connections`() {
        val expectedBrushingMode = BrushingMode.Slow

        val activeConnection1 = KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.ACTIVE)
            .withBrushingMode(listOf(), expectedBrushingMode)
            .build()

        val terminatedConnection = KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.TERMINATED)
            .withBrushingMode(listOf(), expectedBrushingMode)
            .build()

        val activeConnection2 = KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.ACTIVE)
            .withBrushingMode(listOf(), expectedBrushingMode)
            .build()

        prepareToothbrushes(listOf(activeConnection1, terminatedConnection, activeConnection2))

        confirmUserModeUseCase.confirmBrushingModeCompletable(PROFILE_ID, expectedBrushingMode)
            .test()
            .assertComplete()

        verify(activeConnection1.brushingMode()).set(expectedBrushingMode)
        verify(activeConnection2.brushingMode()).set(expectedBrushingMode)
        verify(terminatedConnection.brushingMode(), never()).set(any())
    }

    @Test
    fun `confirmBrushingModeCompletable sets brushing mode on every active connection, even if one throws an error`() {
        val expectedBrushingMode = BrushingMode.Slow

        val activeConnection1 = KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.ACTIVE)
            .withBrushingMode(listOf(), expectedBrushingMode)
            .build()

        val activeConnection2 = KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.ACTIVE)
            .withBrushingMode(listOf(), expectedBrushingMode)
            .build()

        whenever(activeConnection1.brushingMode().set(any())).thenReturn(
            Completable.error(
                TestForcedException()
            )
        )

        prepareToothbrushes(listOf(activeConnection1, activeConnection2))

        confirmUserModeUseCase.confirmBrushingModeCompletable(PROFILE_ID, expectedBrushingMode)
            .test()
            .assertNoErrors()
            .assertComplete()

        verify(activeConnection1.brushingMode()).set(expectedBrushingMode)
        verify(activeConnection2.brushingMode()).set(expectedBrushingMode)
    }

    /*
    Utils
     */

    private fun prepareToothbrushes(connections: List<KLTBConnection> = listOf()) {
        val single = Single.just(connections)
        whenever(toothbrushesUseCase.toothbrushesWithBrushingProgramSupport(PROFILE_ID))
            .thenReturn(single)
    }
}

private val defaultBrushingMode = BrushingMode.Regular

private const val PROFILE_ID = 1986L

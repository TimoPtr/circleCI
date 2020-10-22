/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.toothbrush

import com.kolibree.account.utils.ActiveToothbrushesForProfileUseCase
import com.kolibree.account.utils.ToothbrushesForProfileUseCase
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import org.junit.Assert
import org.junit.Test

internal class ActiveToothbrushesForProfileUseCaseTest : BaseUnitTest() {

    private lateinit var useCase: ActiveToothbrushesForProfileUseCase

    private val toothbrushesForProfileUseCase = mock<ToothbrushesForProfileUseCase>()

    override fun setup() {
        super.setup()

        useCase = ActiveToothbrushesForProfileUseCase(toothbrushesForProfileUseCase)
    }

    @Test
    fun `profileActiveToothbrushes returns 0 if no active connections`() {
        val connection1 = KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.ESTABLISHING)
            .build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.TERMINATING)
            .build()
        val connections = listOf(connection1, connection2)
        whenever(toothbrushesForProfileUseCase.currentProfileToothbrushesOnceAndStream())
            .thenReturn(Flowable.just(connections))

        Assert.assertEquals(0, useCase.activeToothbrushes().size)
    }

    @Test
    fun `profileActiveToothbrushes returns 1 if only 1 active connection`() {
        val connection1 = KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.ACTIVE)
            .build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.TERMINATED)
            .build()
        val connections = listOf(connection1, connection2)
        whenever(toothbrushesForProfileUseCase.currentProfileToothbrushesOnceAndStream())
            .thenReturn(Flowable.just(connections))

        Assert.assertEquals(1, useCase.activeToothbrushes().size)
    }

    @Test
    fun `profileActiveToothbrushes returns 2 if exactly 2 active connections`() {
        val connection1 = KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.ACTIVE)
            .build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.ACTIVE)
            .build()
        val connections = listOf(connection1, connection2)
        whenever(toothbrushesForProfileUseCase.currentProfileToothbrushesOnceAndStream())
            .thenReturn(Flowable.just(connections))

        Assert.assertEquals(2, useCase.activeToothbrushes().size)
    }

    @Test
    fun `profileActiveToothbrushes returns 3 if exactly 3 active connections`() {
        val connection1 = KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.ESTABLISHING)
            .build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.ACTIVE)
            .build()
        val connection3 = KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.TERMINATED)
            .build()
        val connection4 = KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.ACTIVE)
            .build()
        val connection5 = KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.NEW)
            .build()
        val connection6 = KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.ACTIVE)
            .build()
        val connections = listOf(
            connection1,
            connection2,
            connection3,
            connection4,
            connection5,
            connection6
        )
        whenever(toothbrushesForProfileUseCase.currentProfileToothbrushesOnceAndStream())
            .thenReturn(Flowable.just(connections))

        Assert.assertEquals(3, useCase.activeToothbrushes().size)
    }

    @Test
    fun `if exceptions returns empty list`() {
        whenever(toothbrushesForProfileUseCase.currentProfileToothbrushesOnceAndStream())
            .thenThrow(RuntimeException())

        Assert.assertEquals(0, useCase.activeToothbrushes().size)
    }
}

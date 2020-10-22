/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.useractivity

import com.kolibree.account.utils.ActiveToothbrushesForProfileUseCase
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.game.DefaultActivity
import com.kolibree.android.app.ui.game.DefaultUserActivityUseCaseImpl
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.connection.vibrator.Vibrator
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultUserActivityUseCaseImplTest : BaseUnitTest() {

    private val activeToothbrushesForProfileUseCase: ActiveToothbrushesForProfileUseCase = mock()

    private lateinit var useCase: DefaultUserActivityUseCaseImpl

    override fun setup() {
        super.setup()

        useCase = spy(DefaultUserActivityUseCaseImpl(activeToothbrushesForProfileUseCase))
    }

    @Test
    fun `unregister method unregisters current connection`() {
        val connection = mock<KLTBConnection>()
        val vibrator = mock<Vibrator>()
        whenever(connection.vibrator()).thenReturn(vibrator)

        useCase.registeredConnection = connection

        useCase.unregister()

        verify(connection).vibrator()
        verify(connection.vibrator()).unregister(useCase)
    }

    @Test
    fun `defaultActivity returns NoToothbrushConnected if not active connection`() {
        whenever(activeToothbrushesForProfileUseCase.activeToothbrushes())
            .thenReturn(emptyList())

        assertEquals(useCase.defaultActivity(), DefaultActivity.NoToothbrushConnected)
    }

    @Test
    fun `defaultActivity returns CoachPlusActivity if only one active connection`() {
        val connection1 = KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.ACTIVE)
            .build()
        val connections = listOf(connection1)
        whenever(activeToothbrushesForProfileUseCase.activeToothbrushes())
            .thenReturn(connections)

        assertEquals(useCase.defaultActivity(), DefaultActivity.CoachPlusActivity(connection1))
    }

    @Test
    fun `defaultActivity returns MultiToothbrush if more than one active connections`() {
        val connection1 = KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.ACTIVE)
            .build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.ACTIVE)
            .build()
        val connections = listOf(connection1, connection2)
        whenever(activeToothbrushesForProfileUseCase.activeToothbrushes())
            .thenReturn(connections)

        assertEquals(useCase.defaultActivity(), DefaultActivity.MultiToothbrush(connections))
    }
}

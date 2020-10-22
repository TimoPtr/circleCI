/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushsettings.usecase

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import org.junit.Test

internal class RenameToothbrushNameUseCaseTest : BaseUnitTest() {

    private lateinit var useCase: RenameToothbrushNameUseCase

    private val toothbrushRepository: ToothbrushRepository = mock()

    override fun setup() {
        super.setup()

        useCase = RenameToothbrushNameUseCase(toothbrushRepository)
    }

    @Test
    fun `when name is already the same then useCase does nothing`() {
        val mac = "01:22"
        val name = "Hum_TB1"
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withName(name)
            .withMac(mac)
            .withState(KLTBConnectionState.ACTIVE)
            .build()

        useCase.rename(connection, name)
            .test()
            .assertComplete()

        verify(toothbrushRepository, never()).rename(mac, name)
        verify(connection.toothbrush(), never()).setAndCacheName(name)
    }

    @Test
    fun `when connection is not ACTIVE then exception is thrown`() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.TERMINATED)
            .build()

        useCase.rename(connection, "new_name")
            .test()
            .assertError(ToothbrushDisconnectedException)
    }

    @Test
    fun `when name is not the same then update toothbrush name`() {
        val mac = "01:33"
        val newName = "My_Hum_TB"
        whenever(toothbrushRepository.rename(mac, newName)).thenReturn(Completable.complete())
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withName("Hum_TB5")
            .withMac(mac)
            .withState(KLTBConnectionState.ACTIVE)
            .build()
        whenever(connection.toothbrush().setAndCacheName(newName))
            .thenReturn(Completable.complete())

        useCase.rename(connection, newName)
            .test()
            .assertComplete()

        verify(connection.toothbrush()).setAndCacheName(newName)
        verify(toothbrushRepository).rename(mac, newName)
    }
}

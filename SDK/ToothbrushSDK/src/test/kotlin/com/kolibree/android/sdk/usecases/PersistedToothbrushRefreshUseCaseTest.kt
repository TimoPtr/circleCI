/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.usecases

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository
import com.kolibree.android.sdk.version.DspVersion
import com.kolibree.android.sdk.version.SoftwareVersion
import com.kolibree.android.test.mocks.AccountToothbrushBuilder
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Maybe
import io.reactivex.subjects.CompletableSubject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class PersistedToothbrushRefreshUseCaseTest : BaseUnitTest() {
    private val toothbrushRepository: ToothbrushRepository = mock()

    private val useCase = PersistedToothbrushRefreshUseCase(toothbrushRepository)

    private lateinit var connection: KLTBConnection

    @Test
    fun `maybeUpdateVersions completes if there's no persisted toothbrush associated to the connection`() {
        initWithConnection()

        assertBackendNotUpdated {
            whenever(toothbrushRepository.getAccountToothbrush(connection.toothbrush().mac))
                .thenReturn(Maybe.empty())

            useCase.maybeUpdateVersions(connection).test()
        }
    }

    @Test
    fun `maybeUpdateVersions doesn't update the repository if stored values are the same as in toothbrush`() {
        initWithConnection()

        assertBackendNotUpdated {
            val accountToothbrush = AccountToothbrushBuilder.fromConnection(connection)
            whenever(toothbrushRepository.getAccountToothbrush(connection.toothbrush().mac))
                .thenReturn(Maybe.just(accountToothbrush))

            assertTrue(accountToothbrush.name == connection.toothbrush().getName())
            assertTrue(accountToothbrush.serial == connection.toothbrush().serialNumber)
            assertEquals(connection.toothbrush().firmwareVersion, accountToothbrush.firmwareVersion)
            assertEquals(
                connection.toothbrush().bootloaderVersion,
                accountToothbrush.bootloaderVersion
            )

            useCase.maybeUpdateVersions(connection).test()
        }
    }

    @Test
    fun `maybeUpdateVersions updates the repository if stored name is different than the one reported by the connection`() {
        initWithConnection()

        val accountToothbrush = AccountToothbrushBuilder.fromConnection(connection)
            .copy(name = "old name")

        assertTrue(accountToothbrush.name != connection.toothbrush().getName())

        assertAssociatedAndFlaggedAsDirty(connection, accountToothbrush)
    }

    @Test
    fun `maybeUpdateVersions updates the repository if stored serial is different than the one reported by the connection`() {
        initWithConnection()

        val accountToothbrush = AccountToothbrushBuilder.fromConnection(connection)
            .copy(serial = "old serial")

        whenever(toothbrushRepository.getAccountToothbrush(connection.toothbrush().mac))
            .thenReturn(Maybe.just(accountToothbrush))

        assertTrue(accountToothbrush.serial != connection.toothbrush().serialNumber)

        assertAssociatedAndFlaggedAsDirty(connection, accountToothbrush)
    }

    @Test
    fun `maybeUpdateVersions updates the repository if stored firmware is different than the one reported by the connection`() {
        initWithConnection()

        val accountToothbrush = AccountToothbrushBuilder.fromConnection(connection)
            .copy(
                firmwareVersion = SoftwareVersion(
                    KLTBConnectionBuilder.DEFAULT_FW_VERSION_MAJOR - 1,
                    KLTBConnectionBuilder.DEFAULT_FW_VERSION_MINOR,
                    KLTBConnectionBuilder.DEFAULT_FW_VERSION_REVISION
                )
            )

        whenever(toothbrushRepository.getAccountToothbrush(connection.toothbrush().mac))
            .thenReturn(Maybe.just(accountToothbrush))

        assertTrue(connection.toothbrush().firmwareVersion.isNewer(accountToothbrush.firmwareVersion))

        assertAssociatedAndFlaggedAsDirty(connection, accountToothbrush)
    }

    @Test
    fun `maybeUpdateVersions updates the repository if stored bootloader is different than the one reported by the connection`() {
        initWithConnection()

        val accountToothbrush = AccountToothbrushBuilder.fromConnection(connection)
            .copy(
                bootloaderVersion = SoftwareVersion(
                    KLTBConnectionBuilder.DEFAULT_BOOTLOADER_VERSION_MAJOR,
                    KLTBConnectionBuilder.DEFAULT_BOOTLOADER_VERSION_MINOR,
                    KLTBConnectionBuilder.DEFAULT_BOOTLOADER_VERSION_REVISION - 1
                )
            )

        whenever(toothbrushRepository.getAccountToothbrush(connection.toothbrush().mac))
            .thenReturn(Maybe.just(accountToothbrush))

        assertTrue(connection.toothbrush().bootloaderVersion.isNewer(accountToothbrush.bootloaderVersion))

        assertAssociatedAndFlaggedAsDirty(connection, accountToothbrush)
    }

    @Test
    fun `maybeUpdateVersions updates the repository if stored dsp is different than the one reported by the connection`() {
        initWithConnection(KLTBConnectionBuilder.createAndroidLess()
            .withDspVersion(DspVersion(1, 2, 5))
            .build())

        val accountToothbrush = AccountToothbrushBuilder.fromConnection(connection)
            .copy(
                dspVersion = DspVersion(1, 2, 3)
            )

        whenever(toothbrushRepository.getAccountToothbrush(connection.toothbrush().mac))
            .thenReturn(Maybe.just(accountToothbrush))

        assertTrue(connection.toothbrush().dspVersion.isNewer(accountToothbrush.dspVersion))

        assertAssociatedAndFlaggedAsDirty(connection, accountToothbrush)
    }

    @Test
    fun `maybeUpdateVersions never updates the repository if bootloader reported by the connection is NULL`() {
        initWithConnection(
            KLTBConnectionBuilder.createAndroidLess()
                .withBootloaderVersion(SoftwareVersion.NULL)
                .build()
        )

        val accountToothbrush = AccountToothbrushBuilder.fromConnection(connection)
            .copy(bootloaderVersion = KLTBConnectionBuilder.DEFAULT_BOOTLOADER_VERSION)

        whenever(toothbrushRepository.getAccountToothbrush(connection.toothbrush().mac))
            .thenReturn(Maybe.just(accountToothbrush))

        assertBackendNotUpdated {
            useCase.maybeUpdateVersions(connection).test()
        }
    }

    @Test
    fun `maybeUpdateVersions never updates the repository if dsp reported by the connection is NULL`() {
        initWithConnection(
            KLTBConnectionBuilder.createAndroidLess()
                .withDspVersion(DspVersion.NULL)
                .build()
        )

        val accountToothbrush = AccountToothbrushBuilder.fromConnection(connection)
            .copy(dspVersion = DspVersion(1, 2, 3))

        whenever(toothbrushRepository.getAccountToothbrush(connection.toothbrush().mac))
            .thenReturn(Maybe.just(accountToothbrush))

        assertBackendNotUpdated {
            useCase.maybeUpdateVersions(connection).test()
        }
    }

    @Test
    fun `maybeUpdateVersions never updates the repository if firmware reported by the connection is NULL`() {
        initWithConnection(
            KLTBConnectionBuilder.createAndroidLess()
                .withFirmwareVersion(SoftwareVersion.NULL)
                .build()
        )

        val accountToothbrush = AccountToothbrushBuilder.fromConnection(connection)
            .copy(firmwareVersion = KLTBConnectionBuilder.DEFAULT_BOOTLOADER_VERSION)

        whenever(toothbrushRepository.getAccountToothbrush(connection.toothbrush().mac))
            .thenReturn(Maybe.just(accountToothbrush))

        assertBackendNotUpdated {
            useCase.maybeUpdateVersions(connection).test()
        }
    }

    /*
    Utils
     */

    private fun initWithConnection(
        connection: KLTBConnection = KLTBConnectionBuilder.createAndroidLess().build()
    ) {
        this.connection = connection
    }

    private fun assertAssociatedAndFlaggedAsDirty(
        connection: KLTBConnection,
        accountToothbrush: AccountToothbrush
    ) {
        whenever(toothbrushRepository.getAccountToothbrush(connection.toothbrush().mac))
            .thenReturn(Maybe.just(accountToothbrush))

        val dirtySubject = CompletableSubject.create()
        whenever(toothbrushRepository.flagAsDirty(connection.toothbrush().mac))
            .thenReturn(dirtySubject)

        val associateSubject = CompletableSubject.create()
        whenever(
            toothbrushRepository.associate(
                connection.toothbrush(),
                accountToothbrush.profileId,
                accountToothbrush.accountId
            )
        )
            .thenReturn(associateSubject)

        useCase.maybeUpdateVersions(connection).test()

        assertTrue(associateSubject.hasObservers())
        assertFalse(dirtySubject.hasObservers())
        associateSubject.onComplete()

        assertTrue(dirtySubject.hasObservers())
        dirtySubject.onComplete()
    }

    private fun assertBackendNotUpdated(block: () -> Unit) {
        val subject = CompletableSubject.create()
        whenever(toothbrushRepository.flagAsDirty(any()))
            .thenReturn(subject)

        block()

        assertFalse(subject.hasObservers())

        verify(toothbrushRepository).getAccountToothbrush(connection.toothbrush().mac)
        verifyNoMoreInteractions(toothbrushRepository)
    }
}

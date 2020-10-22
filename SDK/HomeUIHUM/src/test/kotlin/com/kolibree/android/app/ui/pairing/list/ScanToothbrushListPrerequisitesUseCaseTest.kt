/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.list

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.pairing.PairingNavigator
import com.kolibree.android.sdk.connection.CheckConnectionPrerequisitesUseCase
import com.kolibree.android.sdk.connection.ConnectionPrerequisitesState
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

class ScanToothbrushListPrerequisitesUseCaseTest : BaseUnitTest() {

    private val navigator: PairingNavigator = mock()
    private val prerequisitesUseCase: CheckConnectionPrerequisitesUseCase = mock()

    private val useCase = ScanToothbrushListPrerequisitesUseCase(navigator, prerequisitesUseCase)

    @Test
    fun `when prerequisitesUseCase returns EnableBluetooth, navigate to enable bluetooth`() {
        whenever(prerequisitesUseCase.checkConnectionPrerequisites())
            .thenReturn(ConnectionPrerequisitesState.BluetoothDisabled)

        useCase.validateOrNavigate()

        verify(navigator).navigateFromScanListToEnableBluetooth()
    }

    @Test
    fun `when prerequisitesUseCase returns LocationPermissionNotGranted, invoke navigateToGrantLocationPermission`() {
        whenever(prerequisitesUseCase.checkConnectionPrerequisites())
            .thenReturn(ConnectionPrerequisitesState.LocationPermissionNotGranted)

        useCase.validateOrNavigate()

        verify(navigator).navigateFromScanListToGrantLocationPermission()
    }

    @Test
    fun `when prerequisitesUseCase returns LocationServiceDisabled, invoke navigateToEnableLocation`() {
        whenever(prerequisitesUseCase.checkConnectionPrerequisites())
            .thenReturn(ConnectionPrerequisitesState.LocationServiceDisabled)

        useCase.validateOrNavigate()

        verify(navigator).navigateFromScanListToEnableLocation()
    }
}

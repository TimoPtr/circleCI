/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.app.location

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.location.LocationStatus
import com.kolibree.android.location.NoAction
import com.kolibree.android.location.RequestPermission
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.ConnectionState
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.ServiceProvider
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Test
import org.mockito.Mock

class LocationActionCheckerImplTest : BaseUnitTest() {
    @Mock
    lateinit var serviceProvider: ServiceProvider

    @Mock
    lateinit var locationEnabledChecker: LocationStatus

    private lateinit var locationForKLTBConnections: LocationActionCheckerImpl

    override fun setup() {
        super.setup()

        locationForKLTBConnections = LocationActionCheckerImpl(serviceProvider, locationEnabledChecker)
    }

    @Test
    fun `enableLocationActionSingle returns NoLocationAction when service has no connections`() {
        setupService()

        locationForKLTBConnections.enableLocationActionSingle().test().assertValue(NoAction)
    }

    @Test
    fun `enableLocationActionSingle returns NoLocationAction when service only has active connections`() {
        setupService(
            listOf(
                mockConnectionWithState(KLTBConnectionState.ACTIVE),
                mockConnectionWithState(KLTBConnectionState.ACTIVE)
            )
        )

        locationForKLTBConnections.enableLocationActionSingle().test().assertValue(NoAction)
    }

    @Test
    fun `enableLocationActionSingle returns value from checker getEnableLocationAction when it has pending connections`() {
        setupService(listOf(mockConnectionWithState(KLTBConnectionState.ESTABLISHING)))

        val expectedLocationAction = RequestPermission
        whenever(locationEnabledChecker.getLocationAction()).thenReturn(expectedLocationAction)

        locationForKLTBConnections.enableLocationActionSingle().test().assertValue(expectedLocationAction)
    }

    private fun setupService(knownConnections: List<KLTBConnection> = emptyList()) {
        val service: KolibreeService = mock()
        whenever(service.knownConnections).thenReturn(knownConnections)
        whenever(serviceProvider.connectOnce()).thenReturn(Single.just(service))
    }

    private fun mockConnectionWithState(state: KLTBConnectionState): KLTBConnection {
        val connection = mock<KLTBConnection>()

        val connectionState = mock<ConnectionState>()
        whenever(connectionState.current).thenReturn(state)
        whenever(connection.state()).thenReturn(connectionState)

        return connection
    }
}

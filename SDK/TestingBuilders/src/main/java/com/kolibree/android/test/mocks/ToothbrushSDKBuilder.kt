package com.kolibree.android.test.mocks

import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.core.ServiceProvider
import com.nhaarman.mockitokotlin2.whenever

/**
 * Do not use it for now
 */
private class ToothbrushSDKBuilder(private val serviceProvider: ServiceProvider) {

    private val service = serviceProvider.connectOnce().blockingGet()

    private var connections = mutableSetOf<KLTBConnection>()

    fun create(serviceProvider: ServiceProvider): ToothbrushSDKBuilder {
        return ToothbrushSDKBuilder(serviceProvider)
    }

    fun withKLTBConnections(vararg connections: KLTBConnection): ToothbrushSDKBuilder {
        this.connections.addAll(connections)

        return this
    }

    /**
     * Given all the parameters, mock the SDK
     */
    fun build() {
        whenever(service.knownConnections).thenReturn(connections.toList())

        connections.forEach { connection ->
            whenever(service.getConnection(connection.toothbrush().mac)).thenReturn(connection)
        }
    }
}

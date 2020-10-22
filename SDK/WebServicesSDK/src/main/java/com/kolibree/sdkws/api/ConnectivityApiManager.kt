package com.kolibree.sdkws.api

import io.reactivex.Single

interface ConnectivityApiManager {

    fun hasConnectivity(): Boolean

    fun <T> syncWhenConnectivityAvailable(): Single<T>
}

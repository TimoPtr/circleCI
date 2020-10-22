package com.kolibree.sdkws.api

import com.kolibree.android.network.api.ApiError
import com.kolibree.android.network.utils.NetworkChecker
import com.kolibree.sdkws.core.SynchronizationScheduler
import io.reactivex.Single
import javax.inject.Inject

internal class ConnectivityApiManagerImpl
@Inject constructor(
    private val synchronizationScheduler: SynchronizationScheduler,
    private val networkChecker: NetworkChecker
) : ConnectivityApiManager {

    override fun hasConnectivity(): Boolean = networkChecker.hasConnectivity()

    override fun <T> syncWhenConnectivityAvailable(): Single<T> {
        synchronizationScheduler.syncWhenConnectivityAvailable()
        return Single.error(ApiError.generateNetworkError())
    }
}

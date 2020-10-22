package com.kolibree.android.sdk.core

import androidx.annotation.Keep
import io.reactivex.Observable
import io.reactivex.Single

@Keep
interface ServiceProvider {
    /**
     * Observable that will notify when [KolibreeService] is available or it's disconnected
     *
     * Callers are responsible for unsubscribing when they no longer need to keep track of
     * the service. That's normally tied to their lifecycle.
     *
     * Failing to unsubscribe will result in a permanent toothbrush icon in the status bar, since we
     * will keep [KolibreeService] alive as long as there's a client bounded
     *
     * @return Observable that will emit a [ServiceProvisionResult], either [ServiceDisconnected]
     * or [ServiceConnected]
     */
    fun connectStream(): Observable<ServiceProvisionResult>

    /**
     * Single that will notify when [KolibreeService] is available
     *
     * Callers are responsible for unsubscribing when they no longer need to keep track of
     * the service. That's normally tied to their lifecycle.
     *
     * Failing to unsubscribe will result in a permanent toothbrush icon in the status bar, since we
     * will keep [KolibreeService] alive as long as there's a client bounded
     *
     * @return Single that will emit [KolibreeService] once it's available. If it never becomes
     * available, it'll never emit
     */
    fun connectOnce(): Single<KolibreeService>

    @Deprecated(
        replaceWith = ReplaceWith("connectOnce"),
        message = "Should be replaced by connectOnce"
    )
    fun provide(): Single<KolibreeService>

    @Deprecated(
        replaceWith = ReplaceWith("connectStream"),
        message = "Should be replaced by connectStream"
    )
    fun connect(): Observable<Boolean>
}

@Keep
sealed class ServiceProvisionResult

@Keep
object ServiceDisconnected : ServiceProvisionResult()

@Keep
data class ServiceConnected(val service: KolibreeService) : ServiceProvisionResult()

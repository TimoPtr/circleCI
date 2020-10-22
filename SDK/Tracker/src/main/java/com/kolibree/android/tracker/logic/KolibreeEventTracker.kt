/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.tracker.logic

import android.app.Activity
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.OnLifecycleEvent
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.ServiceConnected
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.sdk.core.ServiceProvisionResult
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.EventTracker
import com.kolibree.android.tracker.logic.userproperties.UserPropertiesFactory
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository
import com.kolibree.sdkws.core.IKolibreeConnector
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutorService
import javax.inject.Inject
import javax.inject.Provider
import timber.log.Timber

/** Created by Kornel on 3/13/2018.  */
internal class KolibreeEventTracker @Inject constructor(
    private val tracker: AnalyticsTracker,
    private val serviceProvider: ServiceProvider,
    private val connector: IKolibreeConnector,
    private val userPropertiesFactoryProvider: Provider<UserPropertiesFactory>,
    private val brushingsRepository: BrushingsRepository,
    private val eventExecutor: ExecutorService
) : EventTracker {

    private var serviceConnectDisposable: Disposable? = null

    @VisibleForTesting
    var weakService = WeakReference<KolibreeService>(null)

    @VisibleForTesting
    val latestUsedConnection: KLTBConnection?
        get() {
            val service = weakService.get()
            if (service != null && service.knownConnections.isNotEmpty()) {
                val knownConnections = service.knownConnections

                val lastBrushingMac = lastBrushingMac

                for (connection in knownConnections) {
                    if (connection.toothbrush().mac == lastBrushingMac) {
                        return connection
                    }
                }

                return knownConnections[0]
            }

            return null
        }

    /**
     * @return the latest brushing session mac. Null if there's no active profile or no brushing
     * sessions
     */
    private val lastBrushingMac: String?
        get() {
            val currentProfile = connector.currentProfile

            if (currentProfile != null) {
                val lastBrushingSession =
                    brushingsRepository.getLastBrushingSession(currentProfile.id)

                if (lastBrushingSession != null) {
                    return lastBrushingSession.toothbrushMac
                }
            }

            return null
        }

    override fun sendEvent(event: AnalyticsEvent) {
        eventExecutor.submit {
            val userProperties = createUserProperties()
            val details = event.details?.let {
                it + userProperties.toMap()
            } ?: userProperties.toMap()
            tracker.sendEvent(event.name, details)
        }

        Timber.d("sendEvent: $event")
    }

    override fun setCurrentScreen(activity: Activity, screenName: String) {
        eventExecutor.submit {
            tracker.setCurrentScreen(activity, screenName)
        }
        Timber.d("setCurrentScreen: $activity, $screenName")
    }

    @VisibleForTesting
    fun createUserProperties(): UserPropertiesFactory {
        val latestUsedConnection = latestUsedConnection
        val userProperties = userPropertiesFactoryProvider.get()
        userProperties.fill(connector, latestUsedConnection)
        return userProperties
    }

    @OnLifecycleEvent(Event.ON_RESUME)
    fun onResume() {
        if (serviceConnectDisposable == null || serviceConnectDisposable?.isDisposed == true) {
            serviceConnectDisposable = serviceProvider
                .connectStream()
                .subscribeOn(Schedulers.io())
                .doOnDispose { this.onKolibreeServiceDisconnected() }
                .subscribe(
                    { onServiceConnectionChanged(it) },
                    Timber::e
                )
        }
    }

    @OnLifecycleEvent(Event.ON_PAUSE)
    fun onPause() {
        serviceConnectDisposable?.dispose()
        serviceConnectDisposable = null
    }

    private fun onServiceConnectionChanged(serviceProvisionResult: ServiceProvisionResult) {
        if (serviceProvisionResult is ServiceConnected) {

            onKolibreeServiceConnected(serviceProvisionResult.service)
        } else {
            onKolibreeServiceDisconnected()
        }
    }

    @VisibleForTesting
    fun onKolibreeServiceConnected(service: KolibreeService) {
        this.weakService = WeakReference(service)
    }

    private fun onKolibreeServiceDisconnected() {
        weakService = WeakReference<KolibreeService>(null)
    }
}

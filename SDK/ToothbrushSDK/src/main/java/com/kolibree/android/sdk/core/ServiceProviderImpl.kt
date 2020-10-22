/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.dagger.ApplicationContext
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.utils.lifecycle.ApplicationLifecycleUseCase
import io.reactivex.Emitter
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import timber.log.Timber

internal class ServiceProviderImpl constructor(
    private val appContext: ApplicationContext,
    private val applicationLifecycleUseCase: ApplicationLifecycleUseCase,
    private val timeScheduler: Scheduler,
    private val serviceIntent: Intent
) : ServiceProvider {

    /**
     * Allows to monitor how many active service connections we have.
     * Mostly for debugging purposes.
     */
    private val activeConnectionsCount = AtomicInteger()

    @Inject
    constructor(
        appContext: ApplicationContext,
        applicationLifecycleUseCase: ApplicationLifecycleUseCase,
        @SingleThreadScheduler timeScheduler: Scheduler
    ) : this(
        appContext = appContext,
        applicationLifecycleUseCase = applicationLifecycleUseCase,
        timeScheduler = timeScheduler,
        serviceIntent = Intent(appContext, KolibreeService::class.java).apply {
            putExtra(KolibreeService.KOLIBREE_BINDING_EXTRA, true)
        }
    )

    /**
     * Creates new connection stream that will try to connect to
     * [KolibreeService] whenever application is in the foreground.
     *
     * When application goes into the background,
     * stream will terminate after [DELAY_BACKGROUND_CHANGES_SECONDS].
     *
     * All events are emitted on [Schedulers.io].
     * It is very important to jump of from the main thread
     * to avoid deadlocks in dependent components.
     */
    override fun connectStream(): Observable<ServiceProvisionResult> {
        return applicationLifecycleUseCase
            .observeApplicationState()
            .toObservable()
            .map { state -> state.isAtLeast(Lifecycle.State.STARTED) }
            .distinctUntilChanged()
            .delayBackgroundStateChanges()
            .connectWhenInForeground()
            // It is very important to jump off from the main thread.
            .observeOn(Schedulers.io())
    }

    override fun connectOnce(): Single<KolibreeService> {
        return connectStream()
            .filter { result -> result is ServiceConnected }
            .take(1)
            .map { result -> (result as ServiceConnected).service }
            .singleOrError()
    }

    override fun provide(): Single<KolibreeService> {
        return connectOnce()
    }

    override fun connect(): Observable<Boolean> {
        return connectStream()
            .map { result -> result is ServiceConnected }
    }

    private fun Observable<Boolean>.delayBackgroundStateChanges(): Observable<Boolean> {
        return switchMapSingle { isInForeground ->
            if (isInForeground) Single.just(isInForeground)
            else Single.just(isInForeground)
                .delay(DELAY_BACKGROUND_CHANGES_SECONDS, TimeUnit.SECONDS, timeScheduler)
        }
    }

    private fun Observable<Boolean>.connectWhenInForeground(): Observable<ServiceProvisionResult> {
        return switchMap { isInForeground ->
            if (isInForeground) {
                observeConnection()
            } else {
                Observable.just(ServiceDisconnected)
            }
        }
    }

    /**
     * Wraps [ServiceConnection] to [KolibreeService] into RxJava [Observable].
     *
     * Each [Observable] have its own [ServiceConnection].
     * We can do that because [binder is cached by the system:]
     * [https://developer.android.com/guide/components/bound-services]
     *
     * ```
     * You can connect multiple clients to a service simultaneously.
     * However, the system caches the IBinder service communication channel.
     * In other words, the system calls the service's onBind() method to generate
     * the IBinder only when the first client binds. The system then delivers
     * that same IBinder to all additional clients that bind to that same service,
     * without calling onBind() again.
     * ```
     *
     * All events are emitted on the main thread because
     * [ServiceConnection] callbacks are executed on the main thread.
     *
     * @suppress TooGenericExceptionCaught because bindService
     * it not well documented and we don't really know what it may throw.
     */
    @SuppressWarnings("TooGenericExceptionCaught")
    private fun observeConnection() = Observable.create<ServiceProvisionResult> { emitter ->
        val serviceConnection = createConnection(emitter)

        emitter.setCancellable {
            appContext.unbindService(serviceConnection)
            Timber.d("Unbind service. Active connections: ${activeConnectionsCount.decrementAndGet()}")
        }

        try {
            val bound = appContext.bindService(serviceIntent, serviceConnection, SERVICE_FLAGS)
            if (!bound) {
                error("Unable to bind service!")
            }
            Timber.d("Bind service. Active connections: ${activeConnectionsCount.incrementAndGet()}")
        } catch (exception: Exception) {
            emitter.onError(exception)
        }
    }

    private fun createConnection(
        emitter: Emitter<ServiceProvisionResult>
    ) = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            val kolibreeBinder = binder as KolibreeService.KolibreeBinder
            val result = ServiceConnected(kolibreeBinder.service)
            emitter.onNext(result)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            emitter.onNext(ServiceDisconnected)
        }
    }

    companion object {
        private const val DELAY_BACKGROUND_CHANGES_SECONDS = 15L
        private const val SERVICE_FLAGS = Context.BIND_AUTO_CREATE or
            Context.BIND_IMPORTANT or
            Context.BIND_ABOVE_CLIENT
    }
}

/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import com.kolibree.android.glimmer.di.DaggerGlimmerTweakerApplicationComponent
import com.kolibree.android.glimmer.di.GlimmerTweakerApplicationComponent
import com.kolibree.android.glimmer.di.WithConnectionComponent
import com.kolibree.android.sdk.KolibreeAndroidSdk
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.kml.Kml
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject
import timber.log.Timber
import timber.log.Timber.DebugTree

class GlimmerApplication : Application(), HasAndroidInjector {

    @Inject
    internal lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    // Used to keep the BLE service alive
    @Inject
    internal lateinit var serviceProvider: ServiceProvider

    lateinit var appComponent: GlimmerTweakerApplicationComponent
    private var withConnectionComponent: WithConnectionComponent? = null

    private val disposables = CompositeDisposable()

    override fun androidInjector() = AndroidInjector<Any> { instance ->
        val injected = withConnectionComponent?.androidInjector()?.maybeInject(instance)
            ?: dispatchingAndroidInjector.maybeInject(instance)

        if (!injected) throw IllegalStateException("Cant find injector for ${instance::class.java}")
    }

    override fun onCreate() {
        super.onCreate()

        Timber.plant(DebugTree())
        AndroidThreeTen.init(this)
        Kml.init()

        val sdkComponent = KolibreeAndroidSdk.init(
            context = this,
            translationsProvider = null
        )

        appComponent = DaggerGlimmerTweakerApplicationComponent.builder()
            .context(this)
            .app(this)
            .kolibreeSdk(sdkComponent)
            .build()

        appComponent.inject(this)
    }

    override fun onTerminate() {
        disposables.dispose()
        super.onTerminate()
    }

    fun buildWithConnectionComponent(connection: KLTBConnection) {
        withConnectionComponent = appComponent.withConnectionComponent().create(connection)

        disposables.add(serviceProvider.connectStream().subscribe {
            Timber.d(it.toString())
        })
    }
}

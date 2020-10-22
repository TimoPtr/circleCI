package com.kolibree.android.app.ui.activity

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.Keep
import com.kolibree.android.app.interactor.KolibreeServiceInteractor
import com.kolibree.android.app.interactor.LocationActionInteractor
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.ServiceProvider
import dagger.android.AndroidInjection
import javax.inject.Inject

/**
 * Created by aurelien on 07/01/17.
 *
 * Kolibree service interface activity
 *
 * maragues - 05/october/17
 *
 * I've removed the manual binding/unbinding and delegated getting the service to a
 * KolibreeFacade
 *
 * I've also removed the LocalBroadcastListener, I don't think we need it anymore, since in
 * theory KolibreeFacade will notify us whenever the service gets destroyed/reconnected
 *
 * This changes facilitate testing, otherwise there was no way to provide mock KolibreeService or
 * KLTBConnections
 *
 * 31-8-2018
 *
 * In order to extend from this class, add your Activity to BindingModule `
 *
 * 28/03/2019
 *
 * Class was converted to Kotlin and its logic was transferred to KolibreeServiceInteractor.
 * This class should not be used for new games.
 *
 * @ActivityScope
 * @ContributesAndroidInjector abstract MyActivity bindMyActivity();
` *
 */
@Keep
@Deprecated(
    message = "This class is kept only for compatibility purposes. New games should not inherit from it.",
    replaceWith = ReplaceWith("KolibreeServiceInteractor")
)
abstract class KolibreeServiceActivity : BaseActivity(),
    KolibreeServiceInteractor.Listener,
    LocationActionInteractor.Listener {

    @Inject
    open lateinit var serviceProvider: ServiceProvider

    @Inject
    lateinit var kolibreeServiceInteractor: KolibreeServiceInteractor

    @Inject
    lateinit var locationActionInteractor: LocationActionInteractor

    val service: KolibreeService?
        get() = kolibreeServiceInteractor.service

    override fun onCreate(savedInstanceState: Bundle?) {
        // FIXME move injection away from here, to for ex. `BaseDaggerActivity`
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        kolibreeServiceInteractor.shouldConnectToServiceDelegate = shouldConnectToService()
        kolibreeServiceInteractor.setLifecycleOwner(this)
        kolibreeServiceInteractor.addListener(this)
        locationActionInteractor.setLifecycleOwner(this)
        locationActionInteractor.addListener(this)
    }

    override fun onDestroy() {
        locationActionInteractor.removeListener(this)
        kolibreeServiceInteractor.removeListener(this)
        super.onDestroy()
    }

    @Deprecated(
        message = "This is kept for temporary compatibility and will be removed",
        level = DeprecationLevel.WARNING,
        replaceWith = ReplaceWith("nothing")
    )
    protected open fun shouldConnectToService(): () -> Boolean {
        return { true }
    }

    @CallSuper
    override fun onKolibreeServiceConnected(service: KolibreeService) {
        // reserved
    }

    @CallSuper
    override fun onKolibreeServiceDisconnected() {
        // reserved
    }

    @CallSuper
    override fun onEnableLocationActionNeeded() {
        // reserved
    }
}

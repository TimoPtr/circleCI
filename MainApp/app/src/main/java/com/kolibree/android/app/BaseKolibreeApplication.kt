package com.kolibree.android.app

import android.content.Context
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.multidex.MultiDexApplication
import com.jakewharton.threetenabp.AndroidThreeTen
import com.kolibree.android.app.dagger.AppComponent
import com.kolibree.android.app.dagger.DaggerAppComponent
import com.kolibree.android.app.dagger.HasViewInjector
import com.kolibree.android.app.dagger.ViewInjector
import com.kolibree.android.app.initializers.di.AppInitializerList
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.jaws.coach.CoachPlusView
import com.kolibree.android.jaws.color.ColorJawsView
import com.kolibree.android.jaws.guidedbrushing.GuidedBrushingJawsView
import com.kolibree.android.jaws.hum.HumJawsView
import com.kolibree.android.sdk.KolibreeAndroidSdk
import com.kolibree.android.sdk.core.BackgroundJobManager
import com.kolibree.android.tracker.EventTracker
import com.kolibree.game.middleware.GameMiddleware
import com.kolibree.kml.Kml
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject
import kotlin.system.measureTimeMillis
import timber.log.Timber

/**
 * Base application class that all build types will shareoBas
 *
 *
 * Created by miguelaragues on 18/8/17.
 */
abstract class BaseKolibreeApplication : MultiDexApplication(), HasAndroidInjector,
    HasViewInjector {

    @Inject
    internal lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    internal lateinit var backgroundJobManager: BackgroundJobManager

    @Inject
    lateinit var appInitializers: AppInitializerList

    private val viewInjector = AppViewInjector()

    override fun onCreate() {
        super.onCreate()
        val start = System.currentTimeMillis()

        Kml.init()
        GameMiddleware.init()

        initTimezones()

        initDagger()
        initAppInitializers()

        val end = System.currentTimeMillis()
        Timber.d("Application onCreate took ${end - start}ms")
    }

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    override fun <T : View> viewInjector(clazz: Class<T>): ViewInjector<T> =
        viewInjector.viewInjector(clazz)

    /**
     * Initialize timezone info on AndroidThreeTen
     *
     * It needs to happen before Dagger injection or we can crash due to
     * [org.threeten.bp.zone.ZoneRulesException]
     */
    private fun initTimezones() {
        AndroidThreeTen.init(this)
    }

    open fun initDagger() {
        val sdkComponent = KolibreeAndroidSdk.init(
            this,
            null
        )

        appComponent = DaggerAppComponent.builder()
            .context(this)
            .sdkComponent(sdkComponent)
            .build()

        appComponent.inject(this)

        sdkComponent.backgroundJobManagers().add(backgroundJobManager)

        DataBindingUtil.setDefaultComponent(appComponent)
    }

    private fun initAppInitializers() {
        appInitializers
            .forEach { initializer ->
                val initializerTime = measureTimeMillis {
                    initializer.initialize(this)
                }

                Timber
                    .tag(APP_INITIALIZER_TAG)
                    .d("Initialized ${initializer::class.simpleName} in ${initializerTime}ms")
            }
    }

    /** Provider of view injections  */
    private inner class AppViewInjector : HasViewInjector {

        // Return app component for supported views
        override fun <T : View> viewInjector(clazz: Class<T>): ViewInjector<T> =
            object : ViewInjector<T> {
                override fun inject(view: T) {
                    when (clazz) {
                        CoachPlusView::class.java -> {
                            appComponent.inject(view as CoachPlusView)
                        }
                        ColorJawsView::class.java -> {
                            appComponent.inject(view as ColorJawsView)
                        }
                        HumJawsView::class.java -> appComponent.inject(view as HumJawsView)
                        GuidedBrushingJawsView::class.java ->
                            appComponent.inject(view as GuidedBrushingJawsView)
                        else -> {
                            FailEarly.fail("App doesn't have ViewInjector for %s", clazz.name)
                        }
                    }
                }
            }
    }

    companion object {
        @JvmStatic
        lateinit var appComponent: AppComponent

        @JvmStatic
        fun getEventTracker(context: Context): EventTracker = appComponent.eventTracker()

        const val APP_INITIALIZER_TAG = "AppInitializer"
    }
}

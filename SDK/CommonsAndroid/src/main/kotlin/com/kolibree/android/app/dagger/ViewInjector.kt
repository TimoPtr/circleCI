/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.annotation.Keep
import com.kolibree.android.failearly.FailEarly

/**
 * Interface for objects which are able to perform dependency injection for particular type of views
 */
@SuppressLint("DeobfuscatedPublicSdkClass")
interface ViewInjector<T : View> {

    /**
     * Inject dependencies into particular view
     * @param view subject of injection
     */
    fun inject(view: T)
}

/**
 * Interface for objects which are able to provide view injectors for particular type of views
 */
@SuppressLint("DeobfuscatedPublicSdkClass")
interface HasViewInjector {

    /**
     * Get view injector
     */
    fun <T : View> viewInjector(clazz: Class<T>): ViewInjector<T>
}

@Keep
fun <T : View> Context.viewInjectorForViewType(clazz: Class<T>): ViewInjector<T> {
    FailEarly.failInConditionMet(
        this !is HasViewInjector && applicationContext !is HasViewInjector,
        "Either Activity or Application needs to implement HasViewInjector interface to use injection"
    )

    val provider = if (this is HasViewInjector) this else applicationContext as HasViewInjector

    return provider.viewInjector(clazz)
}

@Keep
inline fun <reified T : View> Context.viewInjectorForViewType(): ViewInjector<T> =
    viewInjectorForViewType(T::class.java)

/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.base

import androidx.annotation.Keep
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import kotlin.reflect.KClass

private typealias FactoryProducer = (() -> ViewModelProvider.Factory)?

/**
 * Returns an existing navigator ViewModel or creates a new one
 * and binds it with [OWNER] lifecycle (usually, a fragment or an activity).
 *
 * This function was migrated from inline function
 * to be able to perform compile time check
 * if [NAVIGATOR] supports given [OWNER].
 */
@Keep
fun <OWNER, NAVIGATOR : BaseNavigator<OWNER>> OWNER.createNavigatorAndBindToLifecycle(
    navigatorClass: KClass<NAVIGATOR>,
    factoryProducer: FactoryProducer = null
): NAVIGATOR where OWNER : HasDefaultViewModelProviderFactory, OWNER : ViewModelStoreOwner, OWNER : LifecycleOwner {
    val navigator = viewModelProvider(factoryProducer).get(navigatorClass.java)
    lifecycle.addObserver(navigator)
    return navigator
}

private fun <T> T.viewModelProvider(
    factoryProducer: FactoryProducer
): ViewModelProvider where T : HasDefaultViewModelProviderFactory, T : ViewModelStoreOwner {
    return ViewModelProvider(
        viewModelStore,
        factoryProducer?.invoke() ?: defaultViewModelProviderFactory
    )
}

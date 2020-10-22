/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.base

import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.annotation.Keep
import androidx.lifecycle.ViewModelProvider

/**
 * Extension to create a [BaseViewModel] given a [ComponentActivity] and a [ViewModelProvider.Factory]
 *
 * As opposed to extension functions declared in BaseViewModelFactoryExt, functions defined in this
 * class can be invoked on the ComponentActivity
 */
@Keep
inline fun <reified T : BaseViewModel<*, *>> ComponentActivity.createViewModelAndBindToLifeCycle(
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): T {
    val viewModel: T = viewModels<T>(factoryProducer).value
    lifecycle.addObserver(viewModel)
    return viewModel
}

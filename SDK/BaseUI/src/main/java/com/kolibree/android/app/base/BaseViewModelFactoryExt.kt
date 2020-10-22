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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

@Keep
fun <T : BaseViewModel<*, *>> BaseViewModel.Factory<*>.createAndBindToLifecycle(
    activity: BaseMVIActivity<*, *, *, *, *>,
    viewModelClass: Class<T>
): T = createAndBindToLifecycle(activity, activity.getLifecycle(), viewModelClass)

@Keep
fun <T : BaseViewModel<*, *>> BaseViewModel.Factory<*>.createAndBindToLifecycle(
    fragment: BaseMVIFragment<*, *, *, *, *>,
    viewModelClass: Class<T>
): T = createAndBindToLifecycle(fragment, fragment.getLifecycle(), viewModelClass)

@Keep
fun <T : BaseViewModel<*, *>> BaseViewModel.Factory<*>.createAndBindToLifecycle(
    dialog: BaseMVIDialogFragment<*, *, *, *, *>,
    viewModelClass: Class<T>
): T = createAndBindToLifecycle(dialog, dialog.getLifecycle(), viewModelClass)

private fun <T : BaseViewModel<*, *>> BaseViewModel.Factory<*>.createAndBindToLifecycle(
    viewModelStore: ViewModelStoreOwner,
    lifecycle: Lifecycle,
    viewModelClass: Class<T>
): T {
    val viewModel = ViewModelProvider(viewModelStore, this).get(viewModelClass)
    lifecycle.addObserver(viewModel)
    return viewModel
}

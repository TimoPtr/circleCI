/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.viewmodel

import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders

@Suppress("Unused")
@Keep
object GenericViewModelFactory {

    /**
     * Provide view model using inlined factory - version for fragment
     */
    @Suppress("UNCHECKED_CAST")
    inline fun <reified VM : ViewModel> Fragment.provideViewModel(crossinline createVM: () -> VM): VM =
        ViewModelProviders.of(this, viewModelFactoryFrom(createVM)).get(VM::class.java)

    /**
     * Provide view model using inlined factory - version for activity
     */
    @Suppress("UNCHECKED_CAST")
    inline fun <reified VM : ViewModel> FragmentActivity.provideViewModel(crossinline createVM: () -> VM): VM =
        ViewModelProviders.of(this, viewModelFactoryFrom(createVM)).get(VM::class.java)

    /**
     * Create view model factory from inlined VM creation method
     */
    @Suppress("UNCHECKED_CAST")
    inline fun <T> viewModelFactoryFrom(crossinline createVM: () -> T): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T = createVM() as T
        }
    }
}

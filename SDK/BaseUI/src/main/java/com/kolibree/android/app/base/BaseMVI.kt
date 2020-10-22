/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.base

import android.os.Bundle
import androidx.annotation.LayoutRes

/**
 * This interface contains shared method for MVI implementation
 */
internal interface BaseMVI<VS : BaseViewState, A : BaseAction,
    VMF : BaseViewModel.Factory<VS>,
    VM : BaseViewModel<VS, A>> {

    /**
     * This method should be override to bind parameters from the bundle to the factory
     * it's helpful if you need to pass parameters to the factory which is injected by
     * dagger. It's called just before the viewModel creation in onCreate
     *
     * By default it bind the viewState
     */
    fun bindVariablesToViewModelFactory(factory: VMF, savedInstanceState: Bundle?)

    /**
     * This is a hack to be able to deal with the ViewModelProviders and the VM creation
     */
    fun getViewModelClass(): Class<VM>

    /**
     * This is use to create the binding
     */
    @LayoutRes
    fun getLayoutId(): Int

    /**
     * React from an Action from the ViewModel
     */
    fun execute(action: A)
}

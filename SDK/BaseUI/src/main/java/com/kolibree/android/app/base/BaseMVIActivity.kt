/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.base

import android.content.Context
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.Keep
import androidx.annotation.StyleRes
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PROTECTED
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.kolibree.android.app.ui.activity.BaseDaggerActivity
import com.kolibree.android.baseui.BR
import com.kolibree.android.translationssupport.TranslationContext
import com.kolibree.android.utils.DisposableScope
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

/**
 * This BaseMVIActivity is a base to implement the MVI pattern,
 * after onCreate you will have a instance of viewModel.
 *
 * It implement DelegateSubscription so you can use this activity lifecycle subscription
 */
@Keep
abstract class BaseMVIActivity<VS : BaseViewState,
    A : BaseAction,
    VMF : BaseViewModel.Factory<VS>,
    VM : BaseViewModel<VS, A>,
    B : ViewDataBinding> :
    BaseDaggerActivity(), BaseMVI<VS, A, VMF, VM> {

    companion object {
        private const val VIEW_STATE = "bundle_view_state"
    }

    protected lateinit var viewModel: VM

    @Inject
    internal lateinit var viewModelFactory: VMF

    /**
     * Use this variable to access your views elements it will be available after onCreate
     * has been called
     */
    protected lateinit var binding: B

    /**
     * Disposable scope that is available after onCreate has been called.
     *
     * Cleared in [onDestroy] method.
     */
    @VisibleForTesting
    val onDestroyDisposables = DisposableScope("onDestroy")

    /**
     * Disposable scope that is available after onStart has been called.
     *
     * Cleared in [onStop] method.
     */
    @VisibleForTesting
    val onStopDisposables = DisposableScope("onStop")

    /**
     * Disposable scope that is available after onResume has been called.
     *
     * Cleared in [onPause] method.
     */
    @VisibleForTesting
    val onPauseDisposables = DisposableScope("onPause")

    // Android SDK scope

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onDestroyDisposables.ready()
        maybeApplyConfigurableTheme()
        binding = DataBindingUtil.setContentView(this, getLayoutId())
        binding.lifecycleOwner = this
        bindVariablesToViewModelFactory(viewModelFactory, savedInstanceState)
        createViewModel()
        binding.setVariable(BR.viewModel, viewModel)
    }

    @CallSuper
    override fun onStart() {
        super.onStart()
        onStopDisposables.ready()
    }

    override fun onResume() {
        super.onResume()
        onPauseDisposables.ready()
        disposeOnPause { subscribeToActions(viewModel) }
        viewModel.children.forEach { disposeOnPause { subscribeToActions(it) } }
    }

    override fun onPause() {
        onPauseDisposables.clear()
        super.onPause()
    }

    override fun onStop() {
        onStopDisposables.clear()
        super.onStop()
    }

    override fun onDestroy() {
        onPauseDisposables.dispose()
        onStopDisposables.dispose()
        onDestroyDisposables.dispose()
        super.onDestroy()
    }

    @CallSuper
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(VIEW_STATE, viewModel.getViewState())
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(TranslationContext(newBase))
    }

    // MVI scope

    @CallSuper
    override fun bindVariablesToViewModelFactory(factory: VMF, savedInstanceState: Bundle?) {
        factory.viewState = savedInstanceState?.getParcelable(VIEW_STATE)
    }

    // Internal scope

    private fun createViewModel() {
        viewModel = viewModelFactory.createAndBindToLifecycle(this, getViewModelClass())
    }

    private fun subscribeToActions(viewModel: BaseViewModel<*, out A>): Disposable =
        viewModel.actionsObservable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                this::execute,
                Timber::e
            )

    // Disposables scope

    @VisibleForTesting(otherwise = PROTECTED)
    fun disposeOnPause(block: () -> Disposable?) {
        onPauseDisposables += block()
    }

    @VisibleForTesting(otherwise = PROTECTED)
    fun disposeOnStop(block: () -> Disposable?) {
        onStopDisposables += block()
    }

    @VisibleForTesting(otherwise = PROTECTED)
    fun disposeOnDestroy(block: () -> Disposable?) {
        onDestroyDisposables += block()
    }

    // Theme scope

    @StyleRes
    protected open fun getConfigurableTheme(): Int? = null

    private fun maybeApplyConfigurableTheme() {
        getConfigurableTheme()?.let { setTheme(it) }
    }
}

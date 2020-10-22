/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sba.testbrushing.base.legacy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kolibree.android.app.ui.fragment.BaseDaggerFragment
import com.kolibree.android.auditor.UserStep
import com.kolibree.android.sba.testbrushing.base.NoneAction
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

@Deprecated("Use BaseTestBrushingFragment from MVI package")
internal abstract class LegacyBaseTestBrushingFragment<
    VM : LegacyBaseTestBrushingViewModel<VS>,
    VS : LegacyBaseTestBrushingViewState
    > : BaseDaggerFragment(), UserStep {

    @LayoutRes
    abstract fun layoutId(): Int

    abstract fun render(viewState: VS)

    abstract fun createViewModel(): VM

    lateinit var viewModel: VM

    private var viewStateObservable: Disposable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflateView(inflater, container, layoutId())
        viewModel = createViewModel()
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.addObserver(viewModel)
        subscribeListeners()
    }

    private fun subscribeListeners() {
        viewStateObservable = viewModel.viewStateObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                this::onViewStateChanged,
                Throwable::printStackTrace
            )
    }

    private fun onViewStateChanged(viewState: VS) {
        render(viewState)
        if (viewState.action !is NoneAction) {
            viewModel.resetAction()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unsubscribeListeners()
        lifecycle.removeObserver(viewModel)
    }

    private fun unsubscribeListeners() {
        viewStateObservable?.dispose()
    }
}

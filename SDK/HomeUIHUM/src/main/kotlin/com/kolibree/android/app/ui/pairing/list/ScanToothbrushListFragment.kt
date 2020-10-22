/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.list

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.insets.bottomNavigationBarInset
import com.kolibree.android.app.insets.topStatusBarWindowInset
import com.kolibree.android.app.insets.withWindowInsetsOwner
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.FragmentScanToothbrushListBinding
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber

internal class ScanToothbrushListFragment :
    BaseMVIFragment<ScanToothbrushListViewState,
        ScanToothbrushListActions,
        ScanToothbrushListViewModel.Factory,
        ScanToothbrushListViewModel,
        FragmentScanToothbrushListBinding>(),
    TrackableScreen {

    companion object {
        @JvmStatic
        fun newInstance(): ScanToothbrushListFragment = ScanToothbrushListFragment()
    }

    // This callback will catch onBackPressed only when NoBrushFound is displayed
    private val onBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            if (viewModel.getViewState()?.showNoBrushFound == true) {
                viewModel.closeClick()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        disposeOnDestroy {
            viewModel.viewStateFlowable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onBackPressedCallback.isEnabled = it.showNoBrushFound
                }, Timber::e)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val restyledInflater = LayoutInflater.from(
            ContextThemeWrapper(requireContext(), R.style.AppTheme_Light)
        )
        val view = super.onCreateView(restyledInflater, container, savedInstanceState)
        withWindowInsetsOwner { insets ->
            view?.setPadding(0, insets.topStatusBarWindowInset(), 0, insets.bottomNavigationBarInset())
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        requireActivity().window.decorView.systemUiVisibility += View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

    override fun onPause() {
        requireActivity().window.decorView.systemUiVisibility -= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        super.onPause()
    }

    override fun getViewModelClass(): Class<ScanToothbrushListViewModel> =
        ScanToothbrushListViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_scan_toothbrush_list

    override fun execute(action: ScanToothbrushListActions) {
        // no-op
    }

    override fun getScreenName(): AnalyticsEvent = ScanToothbrushListAnalytics.main()
}

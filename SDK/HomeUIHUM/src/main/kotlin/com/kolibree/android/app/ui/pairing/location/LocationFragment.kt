/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.location

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.kolibree.android.app.ui.AnimatedBottomGroupFragment
import com.kolibree.android.app.widget.AnimatorGroup
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.FragmentLocationBinding
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen
import javax.inject.Inject

internal class LocationFragment :
    AnimatedBottomGroupFragment<
        LocationViewState,
        LocationActions,
        LocationViewModel.Factory,
        LocationViewModel,
        FragmentLocationBinding>(),
    TrackableScreen {

    companion object {
        @JvmStatic
        fun newInstance(): LocationFragment = LocationFragment()

        fun createArguments(
            screenType: LocationScreenType,
            popToScanListOnSuccess: Boolean = false
        ): Bundle {
            return Bundle().apply {
                putParcelable(EXTRA_SCREEN_TYPE, screenType)
                putBoolean(EXTRA_POP_TO_SCAN_LIST_ON_SUCCESS, popToScanListOnSuccess)
            }
        }
    }

    @Inject
    lateinit var locationNavigator: LocationNavigator

    override fun getViewModelClass(): Class<LocationViewModel> = LocationViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_location

    override fun execute(action: LocationActions) {
        when (action) {
            is LocationActions.RequestLocationPermission -> {
                askLocationPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    fun extractScreenType(): LocationScreenType {
        return arguments?.getParcelable(EXTRA_SCREEN_TYPE)
            ?: throw IllegalStateException("Missing screen type argument")
    }

    fun extractPopToScanListOnSuccess(): Boolean {
        return arguments?.getBoolean(EXTRA_POP_TO_SCAN_LIST_ON_SUCCESS)
            ?: throw IllegalStateException("Missing PopToScanListOnSuccess argument")
    }

    override fun getScreenName(): AnalyticsEvent = when (extractScreenType()) {
        LocationScreenType.EnableLocation -> LocationAnalytics.enableLocationMain()
        else -> LocationAnalytics.grantLocationPermissionMain()
    }

    override fun animatedBottomGroup(): AnimatorGroup = binding.bottomAnimatorGroup

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        locationNavigator.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private val askLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isPermissionGranted ->
            if (isPermissionGranted) {
                viewModel.onLocationPermissionGranted()
            } else {
                viewModel.onLocationPermissionDenied()
            }
        }

    fun onLocationSettingsClose() = viewModel.onLocationSettingsClose()
}

private const val EXTRA_SCREEN_TYPE = "EXTRA_SCREEN_TYPE"
private const val EXTRA_POP_TO_SCAN_LIST_ON_SUCCESS = "EXTRA_POP_ON_SUCCESS"

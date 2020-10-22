/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.kolibree.android.app.base.BaseNavigator
import com.kolibree.android.app.ui.extention.showInBrowser
import com.kolibree.android.app.ui.pairing.enablebluetooth.EnableBluetoothFragment
import com.kolibree.android.app.ui.pairing.location.LocationFragment
import com.kolibree.android.app.ui.pairing.location.LocationScreenType.EnableLocation
import com.kolibree.android.app.ui.pairing.location.LocationScreenType.GrantLocationPermission
import com.kolibree.android.app.utils.navigateSafe
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.homeui.hum.R
import zendesk.support.Support
import zendesk.support.guide.ViewArticleActivity

@Suppress("TooManyFunctions")
internal class PairingNavigator : BaseNavigator<AppCompatActivity>() {
    fun navigateToBrushFound() {
        navigateTo(R.id.action_fragment_wake_your_brush_to_fragment_brush_found)
    }

    fun navigateFromBrushFoundToSignUp() {
        navigateTo(R.id.action_fragment_brush_found_to_fragment_sign_up)
    }

    fun navigateFromWakeYourBrushToEnableBluetooth() {
        navigateTo(R.id.action_fragment_wake_your_brush_to_enable_bluetooth)
    }

    fun navigateFromEnableBluetoothToWakeYourBrush() {
        navigateTo(R.id.action_fragment_enable_bluetooth_to_wake_your_brush)
    }

    fun navigateFromWakeYourBrushToGrantLocationPermission() = navigateTo(
        navId = R.id.action_fragment_wake_your_brush_to_location,
        arguments = LocationFragment.createArguments(GrantLocationPermission)
    )

    fun navigateFromWakeYourBrushToEnableLocation() = navigateTo(
        navId = R.id.action_fragment_wake_your_brush_to_location,
        arguments = LocationFragment.createArguments(EnableLocation)
    )

    fun navigateFromScanListToEnableBluetooth() {
        navigateTo(
            R.id.action_scan_list_to_enable_bluetooth,
            arguments = EnableBluetoothFragment.createArguments(popToScanListOnSuccess = true)
        )
    }

    fun navigateFromScanListToGrantLocationPermission() = navigateTo(
        navId = R.id.action_scan_list_to_location,
        arguments = LocationFragment.createArguments(
            screenType = GrantLocationPermission,
            popToScanListOnSuccess = true
        )
    )

    fun navigateFromScanListToEnableLocation() = navigateTo(
        navId = R.id.action_scan_list_to_location,
        arguments = LocationFragment.createArguments(
            screenType = EnableLocation,
            popToScanListOnSuccess = true
        )
    )

    fun navigateToIsBrushReady() {
        navigateTo(R.id.action_fragment_wake_your_brush_to_is_brush_ready)
    }

    fun navigateFromIsBrushReadyToWakeYourBrush() {
        navigateTo(R.id.action_fragment_is_brush_ready_to_wake_your_brush)
    }

    fun navigateFromLocationToWakeYourBrush() {
        navigateTo(R.id.action_fragment_location_to_wake_your_brush)
    }

    fun navigateFromBrushFoundToWakeYourBrush() {
        navigateTo(R.id.action_fragment_brush_found_to_wake_your_brush)
    }

    fun navigateToNeedMoreHelp() {
        withOwner {
            getString(R.string.zendesk_articles_can_not_connect).toLongOrNull()?.let { articlesId ->
                FailEarly.failInConditionMet(
                    !Support.INSTANCE.isInitialized,
                    "Zendesk is not initialized"
                )
                ViewArticleActivity.builder(articlesId)
                    .withContactUsButtonVisible(false)
                    .show(this)
            } ?: FailEarly.fail("articlesId invalid")
        }
    }

    fun navigateToToothbrushModelMismatch() {
        navigateTo(R.id.action_fragment_brush_found_to_fragment_model_mismatch)
    }

    fun navigateFromModelMismatchToSignUp() {
        navigateTo(R.id.action_fragment_model_mismatch_to_fragment_sign_up)
    }

    fun navigateToSecondAppPlayStore() {
        withOwner {
            showInBrowser(getString(R.string.change_app_play_store_url))
        }
    }

    fun navigateToScanList() {
        navigateTo(R.id.action_fragment_brush_found_to_scan_list)
    }

    fun navigateFromScanListToSignUp() {
        navigateTo(R.id.action_scan_list_to_fragment_sign_up)
    }

    fun navigateFromScanListToModelMismatch() {
        navigateTo(R.id.action_scan_list_to_fragment_model_mismatch)
    }

    fun navigateFromScanListToWakeYourBrush() {
        navigateTo(R.id.action_scan_list_to_wake_your_brush)
    }

    fun popToScanList() = withOwner {
        findNavController(R.id.nav_host_fragment).popBackStack(R.id.fragment_scan_results, false)
    }

    private fun navigateTo(@IdRes navId: Int, arguments: Bundle? = null) = withOwner {
        findNavController(R.id.nav_host_fragment).navigateSafe(navId, arguments)
    }

    fun finishFlow() {
        withOwner {
            finish()
        }
    }
}

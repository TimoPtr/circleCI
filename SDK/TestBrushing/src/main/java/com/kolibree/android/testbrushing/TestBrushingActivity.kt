/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.testbrushing

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowInsets
import com.google.common.base.Optional
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.Error
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.insets.WindowInsetsMediator
import com.kolibree.android.app.insets.WindowInsetsOwner
import com.kolibree.android.app.insets.topStatusBarWindowInset
import com.kolibree.android.app.widget.snackbar.showErrorSnackbar
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.testbrushing.databinding.ActivityTestBrushingBinding
import com.kolibree.android.tracker.Analytics
import com.kolibree.android.tracker.NonTrackableScreen

@VisibleForApp
class TestBrushingActivity : BaseMVIActivity<
    TestBrushingViewState,
    TestBrushingActions,
    TestBrushingViewModel.Factory,
    TestBrushingViewModel,
    ActivityTestBrushingBinding>(),
    NonTrackableScreen, WindowInsetsOwner {

    private lateinit var windowInsetMediator: WindowInsetsMediator

    override fun onCreate(savedInstanceState: Bundle?) {
        setFullScreenDecorView()
        super.onCreate(savedInstanceState)
        windowInsetMediator = WindowInsetsMediator(binding.root)
        windowInsetMediator.withWindowInsets { insets ->
            binding.humTestBrushingContainer.setPadding(0, insets.topStatusBarWindowInset(), 0, 0)
        }
    }

    override fun withWindowInsets(block: (WindowInsets) -> Unit) {
        windowInsetMediator.withWindowInsets(block)
    }

    override fun getViewModelClass(): Class<TestBrushingViewModel> =
        TestBrushingViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_test_brushing

    override fun onBackPressed() {
        Analytics.send(TestBrushingAnalytics.quit())
        finish()
    }

    override fun execute(action: TestBrushingActions) {
        when (action) {
            is TestBrushingActions.ShowError -> showErrorSnackbar(action.error)
        }
    }

    private fun showErrorSnackbar(error: Error) {
        binding.navHostFragment.showErrorSnackbar(error)
    }

    fun extractMac(): Optional<String> = Optional.of(requireNotNull(
        intent.getStringExtra(EXTRA_TOOTHBRUSH_MAC),
        { "Test brushing doesn't support manual mode, you need to provide TB MAC address" }
    ))

    fun extractModel(): ToothbrushModel {
        val modelName = intent.getStringExtra(EXTRA_TOOTHBRUSH_MODEL)
        return requireNotNull(
            ToothbrushModel.getModelByInternalName(modelName),
            { "Test brushing doesn't support manual mode, you need to provide TB model" }
        )
    }

    internal companion object {

        val TAG = TestBrushingActivity::class.java.simpleName
        val EXTRA_TOOTHBRUSH_MAC = "${TAG}_EXTRA_TOOTHBRUSH_MAC"
        val EXTRA_TOOTHBRUSH_MODEL = "${TAG}_EXTRA_TOOTHBRUSH_MODEL"
    }
}

@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
@VisibleForApp
fun startHumTestBrushingIntent(
    context: Context,
    mac: String,
    model: ToothbrushModel
) = Intent(context, TestBrushingActivity::class.java).also { intent ->
    intent.putExtra(TestBrushingActivity.EXTRA_TOOTHBRUSH_MODEL, model.internalName)
    intent.putExtra(TestBrushingActivity.EXTRA_TOOTHBRUSH_MAC, mac)
}

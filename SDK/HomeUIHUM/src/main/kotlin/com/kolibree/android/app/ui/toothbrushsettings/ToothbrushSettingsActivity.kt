/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushsettings

import android.content.Context
import android.content.Intent
import android.view.Gravity
import androidx.annotation.Keep
import com.google.android.material.snackbar.Snackbar
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.ui.dialog.alertDialog
import com.kolibree.android.app.ui.dialog.textInputDialog
import com.kolibree.android.app.ui.toothbrushsettings.validator.BrushNameValidator
import com.kolibree.android.app.widget.snackbar.snackbar
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.ActivityToothbrushSettingsBinding
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen
import java.lang.IllegalStateException

internal class ToothbrushSettingsActivity :
    BaseMVIActivity<
        ToothbrushSettingsViewState,
        ToothbrushSettingsActions,
        ToothbrushSettingsViewModel.Factory,
        ToothbrushSettingsViewModel,
        ActivityToothbrushSettingsBinding>(),
    TrackableScreen {

    companion object {
        fun createWitMacAddress(context: Context, mac: String): Intent =
            Intent(context, ToothbrushSettingsActivity::class.java).apply {
                putExtra(INTENT_TOOTHBRUSH_MAC, mac)
            }
    }

    override fun getViewModelClass(): Class<ToothbrushSettingsViewModel> =
        ToothbrushSettingsViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_toothbrush_settings

    override fun execute(action: ToothbrushSettingsActions) {
        when (action) {
            is ToothbrushSettingsActions.ShowEditBrushNameDialog ->
                showEditBrushNameDialog(action.currentName)
            is ToothbrushSettingsActions.SomethingWrongHappened -> showSnackbarError()
            is ToothbrushSettingsActions.ShowForgetToothbrushDialog -> showForgetToothbrushDialog()
            is ToothbrushSettingsActions.ConnectNewToothbrush -> showConnectNewToothbrushDialog(
                action.toothbrushName
            )
        }
    }

    override fun onBackPressed() = viewModel.onBackPressed()

    override fun getScreenName(): AnalyticsEvent = ToothbrushSettingsAnalytics.main()

    fun toothbrushMac(): String = readMacFromIntent() ?: throw IllegalStateException()

    private fun showSnackbarError() {
        snackbar(binding.tbSettingsRecyclerView) {
            duration(Snackbar.LENGTH_SHORT)
            message(R.string.something_went_wrong)
        }.show()
    }

    @Suppress("LongMethod")
    private fun showForgetToothbrushDialog() {
        alertDialog(this) {
            lifecycleOwner(this@ToothbrushSettingsActivity)
            title(R.string.tb_settings_forget_tb)
            containedButton {
                title(R.string.ok)
                action {
                    dismiss()
                    viewModel.forgetToothbrush()
                }
            }
            textButton {
                title(R.string.cancel)
                action {
                    ToothbrushSettingsAnalytics.forgetToothbrushCancel()
                    dismiss()
                }
            }
        }.show()
    }

    @Suppress("LongMethod")
    private fun showEditBrushNameDialog(currentName: String) {
        textInputDialog(this, currentName) {
            lifecycleOwner(this@ToothbrushSettingsActivity)
            title(R.string.tb_settings_nickname_title)
            textInput {
                hintText(R.string.tb_settings_edit_nickname_dialog_hint)
                valueText(currentName)
                containedButton {
                    title(R.string.save_changes)
                    valueListener { value -> isEnabled = BrushNameValidator.isValid(value) }
                    action { value ->
                        viewModel.userRenamedToothbrush(value)
                        dismiss()
                    }
                }
                textButton {
                    title(R.string.cancel)
                    action {
                        viewModel.userCancelRenamedToothbrush()
                        dismiss()
                    }
                }
            }
        }.show()
    }

    @Suppress("LongMethod")
    private fun showConnectNewToothbrushDialog(toothbrushName: String) {
        alertDialog(this) {
            lifecycleOwner(this@ToothbrushSettingsActivity)
            featureIcon {
                drawable(R.drawable.ic_delete_toothbrush)
            }
            headlineText {
                text(toothbrushName)
                gravity(Gravity.CENTER)
            }
            body(R.string.tb_settings_connect_new_tb_forget_old_one, Gravity.CENTER)
            iconContainedButton {
                title(R.string.tb_settings_forget_tb)
                icon(R.drawable.ic_trash)
                action {
                    dismiss()
                    viewModel.connectNewBrush()
                }
            }
            textButton {
                title(R.string.cancel)
                action {
                    ToothbrushSettingsAnalytics.popupForgetBrushCancel()
                    dismiss()
                }
            }
        }.show()
    }
}

@Keep
fun toothbrushSettingsIntent(context: Context, mac: String): Intent {
    return ToothbrushSettingsActivity.createWitMacAddress(context, mac)
}

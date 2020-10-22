/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.ui.dialog.alertDialog
import com.kolibree.android.app.ui.dialog.birthDateDialog
import com.kolibree.android.app.ui.dialog.durationDialog
import com.kolibree.android.app.ui.dialog.singleSelectDialog
import com.kolibree.android.app.ui.dialog.textInputDialog
import com.kolibree.android.app.ui.extention.afterMeasured
import com.kolibree.android.app.ui.settings.SettingsActions.SingleSelect
import com.kolibree.android.app.widget.snackbar.snackbar
import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.ActivitySettingsBinding
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen
import com.kolibree.sdkws.brushing.wrapper.IBrushing.Companion.BRUSHING_GOAL_TIME_STEP_SECONDS
import com.kolibree.sdkws.brushing.wrapper.IBrushing.Companion.MAXIMUM_BRUSHING_GOAL_TIME_SECONDS
import com.kolibree.sdkws.brushing.wrapper.IBrushing.Companion.MINIMUM_BRUSHING_GOAL_TIME_SECONDS
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.threeten.bp.Duration
import org.threeten.bp.temporal.ChronoUnit

internal class SettingsActivity :
    BaseMVIActivity<SettingsViewState,
        SettingsActions,
        SettingsViewModel.Factory,
        SettingsViewModel,
        ActivitySettingsBinding>(),
    TrackableScreen {

    override fun getViewModelClass(): Class<SettingsViewModel> = SettingsViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_settings

    override fun execute(action: SettingsActions) = when (action) {
            is SettingsActions.ScrollToPosition -> scrollToPosition(action.position)
            is SettingsActions.ShowLogoutConfirmationDialog -> showLogoutConfirmationDialog()
            is SettingsActions.ShowDeleteAccountConfirmationDialog -> showDeleteAccountConfirmationDialog()
            is SettingsActions.ShowDeleteAccountError -> showDeleteAccountError()
            is SettingsActions.EditText -> showEditTextDialog(action)
            is SingleSelect<*> -> showSelectOptionDialog(action)
            is SettingsActions.ShowGetMyDataDialog -> showGetMyDataDialog(action.sentTo)
            is SettingsActions.EditBrushingDuration -> showEditDurationDialog(action)
            is SettingsActions.EditBirthDate -> showEditBirthDateDialog(action)
    }

    private fun scrollToPosition(position: Int) {
        lifecycleScope.launch {
            binding.settingsRecyclerView.afterMeasured {
                (layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(
                    position,
                    resources.getDimension(R.dimen.dot).toInt()
                )
            }
            delay(elevationRecalculationDelay.toMillis())
            binding.elevator.recalculateElevation()
        }
    }

    private fun showDeleteAccountError() {
        snackbar(binding.settingsRecyclerView) {
            duration(Snackbar.LENGTH_SHORT)
            message(R.string.something_went_wrong)
        }.show()
    }

    @Suppress("LongMethod")
    private fun showDeleteAccountConfirmationDialog() {
        alertDialog(this) {
            lifecycleOwner(this@SettingsActivity)
            title(R.string.settings_delete_account)
            body(R.string.settings_delete_account_popup_message)
            containedButton {
                title(R.string.settings_delete_account_popup_yes)
                action {
                    viewModel.userConfirmedDeleteAccount()
                    dismiss()
                }
            }
            outlinedButton {
                title(R.string.cancel)
                action {
                    SettingsDeleteAccountEventTracker.deleteAccountNo()
                    dismiss()
                }
            }
        }.show()
    }

    private fun showGetMyDataDialog(sentTo: String?) {
        alertDialog(this) {
            lifecycleOwner(this@SettingsActivity)
            title(R.string.settings_get_my_data_popup_title)
            body(getString(R.string.settings_get_my_data_popup_message, sentTo ?: ""))
            containedButton {
                title(R.string.ok)
                action {
                    SettingsAdminEventTracker.getMyDataSuccess()
                    dismiss()
                }
            }
        }.show()
    }

    override fun onBackPressed() {
        viewModel.onCloseClick()
    }

    @Suppress("LongMethod")
    private fun showLogoutConfirmationDialog() {
        alertDialog(this) {
            lifecycleOwner(this@SettingsActivity)
            title(R.string.settings_logout_title)
            body(R.string.settings_logout_message)
            containedButton {
                title(R.string.settings_logout_title)
                action {
                    viewModel.userConfirmedLogout()
                    dismiss()
                }
            }
            outlinedButton {
                title(R.string.cancel)
                action {
                    SettingsLogOutEventTracker.logOutNo()
                    dismiss()
                }
            }
        }.show()
    }

    @Suppress("LongMethod")
    private fun showEditTextDialog(editText: SettingsActions.EditText) {
        textInputDialog(this, editText.currentValue ?: "") {
            lifecycleOwner(this@SettingsActivity)
            editText.title?.also { title(it) }
            textInput {
                editText.hintText?.also { hintText(it) }
                editText.currentValue?.also { valueText(it) }
                containedButton {
                    title(R.string.save_changes)
                    valueListener { value ->
                        isEnabled = value.isNotEmpty()
                    }
                    action { value ->
                        editText.action(value)
                        dismiss()
                        SettingsFirstNameEventTracker.firstNameCancel()
                    }
                }
                textButton {
                    title(R.string.cancel)
                    action { dismiss() }
                }
            }
        }.show()
    }

    @Suppress("LongMethod")
    private fun <T> showSelectOptionDialog(action: SingleSelect<T>) {
        var selectedIndex: Int? = null
        singleSelectDialog(this) {
            lifecycleOwner(this@SettingsActivity)
            action.titleRes?.also { title(it) }
            selectAction {
                selectedIndex = it.index
            }
            action.options.forEach { option ->
                selection {
                    title(option.nameRes)
                    selected = option.value?.equals(action.currentOption) ?: false
                }
            }
            containedButton {
                title(R.string.save_changes)
                action {
                    selectedIndex?.also {
                        action.saveAction(action.options[it].value)
                    }
                    dismiss()
                }
            }
            textButton {
                title(R.string.cancel)
                action {
                    selectDialogDissmised(action)
                    dismiss()
                }
            }
        }.show()
    }

    private fun selectDialogDissmised(action: SingleSelect<*>) {
        when (action.options.firstOrNull()?.value) {
            is Gender -> SettingsProfileEventTracker.closeGenderDialog()
        }
    }

    @Suppress("LongMethod")
    private fun showEditDurationDialog(editBrushingDuration: SettingsActions.EditBrushingDuration) {
        durationDialog(this, editBrushingDuration.currentValue) {
            lifecycleOwner(this@SettingsActivity)
            title(R.string.settings_brushing_time)
            majorLabel(R.string.settings_brushing_time_minutes_label)
            minorLabel(R.string.settings_brushing_time_seconds_label)
            setRange(
                Duration.ofSeconds(MINIMUM_BRUSHING_GOAL_TIME_SECONDS.toLong()),
                Duration.ofSeconds(MAXIMUM_BRUSHING_GOAL_TIME_SECONDS.toLong()),
                Duration.ofSeconds(BRUSHING_GOAL_TIME_STEP_SECONDS.toLong())
            )
            containedButton {
                title(R.string.save_changes)
                action { newValue ->
                    editBrushingDuration.action(newValue)
                    SettingsBrushTimerEventTracker.setBrushTimer()
                    dismiss()
                }
            }
            textButton {
                title(R.string.cancel)
                action {
                    SettingsBrushTimerEventTracker.closeBrushTimerDialog()
                    dismiss()
                }
            }
        }.show()
    }

    @Suppress("LongMethod")
    private fun showEditBirthDateDialog(editBirthDate: SettingsActions.EditBirthDate) {
        birthDateDialog(this, editBirthDate.currentValue) {
            title(R.string.settings_born_header)
            containedButton {
                title(R.string.save_changes)
                action { newValue ->
                    editBirthDate.action(newValue)
                    SettingsProfileEventTracker.setAge()
                    dismiss()
                }
            }
            textButton {
                title(R.string.cancel)
                action {
                    SettingsProfileEventTracker.closeAgeDialog()
                    dismiss()
                }
            }
        }.show()
    }

    override fun getScreenName(): AnalyticsEvent = SettingsAdminEventTracker.main()

    fun getInitialAction(): SettingsInitialAction? = intent.getParcelableExtra(EXTRA_INITIAL_ACTION)

    companion object {

        private const val DELAY_MILLIS = 200L
        private val elevationRecalculationDelay = Duration.of(DELAY_MILLIS, ChronoUnit.MILLIS)
    }
}

@Keep
fun startSettingsIntent(context: Context, initialAction: SettingsInitialAction? = null) {
    val intent = Intent(context, SettingsActivity::class.java)
    initialAction?.let { intent.putExtra(EXTRA_INITIAL_ACTION, initialAction as Parcelable) }
    context.startActivity(intent)
}

private const val EXTRA_INITIAL_ACTION = "EXTRA_INITIAL_ACTION"

/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.secret

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.annotation.IdRes
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.kolibree.R
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.crashlogger.CrashLogger
import com.kolibree.android.app.sample.showcase.ThemeShowcaseActivity
import com.kolibree.android.app.sample.showcase.startSpeedometerPlaygroundIntent
import com.kolibree.android.app.ui.activity.startActivity
import com.kolibree.android.app.ui.chart.startChartPlaygroundIntent
import com.kolibree.android.app.ui.dialog.alertDialog
import com.kolibree.android.app.ui.selecttoothbrush.SelectToothbrushUseCase
import com.kolibree.android.app.ui.settings.secret.badges.startBadgesPlaygroundActivity
import com.kolibree.android.app.ui.settings.secret.dialogs.startDialogsPlaygroundActivity
import com.kolibree.android.app.ui.settings.secret.environment.ShowConfirmChangeEnvironmentAction
import com.kolibree.android.app.ui.settings.secret.environment.ShowCustomEnvironmentMissingFieldErrorAction
import com.kolibree.android.app.ui.settings.secret.environment.ShowCustomEnvironmentSomethingWrongAction
import com.kolibree.android.app.ui.settings.secret.environment.ShowCustomEnvironmentUrlExistsAction
import com.kolibree.android.app.ui.settings.secret.fakebrushings.startCreateFakeBrushingIntent
import com.kolibree.android.app.ui.settings.secret.shape.ShapeSpikeActivity
import com.kolibree.android.app.unity.launchUnityNextGenPlaygroundActivity
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.network.environment.Environment
import com.kolibree.android.tracker.NonTrackableScreen
import com.kolibree.android.ui.settings.SecretSettingsManager
import com.kolibree.databinding.ActivitySecretSettingsBinding
import com.kolibree.databinding.playground.lottie.startLottiePlaygroundIntent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

internal class SecretSettingsActivity : BaseMVIActivity<
    SecretSettingsViewState,
    SecretSettingsBaseAction,
    SecretSettingsViewModel.Factory,
    SecretSettingsViewModel,
    ActivitySecretSettingsBinding>(),
    NonTrackableScreen {

    @Inject
    lateinit var secretSettingsManager: SecretSettingsManager

    @Inject
    lateinit var forceSyncUseCase: ForceSyncUseCase

    @Inject
    lateinit var selectToothbrushUseCase: SelectToothbrushUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.quickActionClickListener = View.OnClickListener { v -> onQuickActionClick(v.id) }
    }

    override fun getViewModelClass(): Class<SecretSettingsViewModel> =
        SecretSettingsViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_secret_settings

    override fun execute(action: SecretSettingsBaseAction) {
        when (action) {
            is ShowAppRestartAction -> showAppRestartSnackbar()
            is ShowOperationFailedAction -> showOperationFailedSnackbar()
            is ShowFeatureEditDialog -> showFeatureEditDialog(action.descriptor)
            is ShowConfirmChangeEnvironmentAction -> showConfirmChangeEnvironmentDialog(action.environment)
            is ShowCustomEnvironmentMissingFieldErrorAction -> showSnackbar("All custom environment fields are needed")
            is ShowCustomEnvironmentUrlExistsAction -> showSnackbar("Custom endpoint url already exists")
            is ShowCustomEnvironmentSomethingWrongAction -> showSnackbar(getString(R.string.something_went_wrong))
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> showFeatureEditDialog(descriptor: FeatureToggleDescriptor<T>) {
        when (descriptor.feature.type()) {
            // Booleans are not handled this way, that's why we skip it
            String::class -> showTextEditDialog(descriptor as FeatureToggleDescriptor<String>)
            Long::class -> showNumericEditDialog(descriptor as FeatureToggleDescriptor<Long>)
            // TODO add support for single-choice enums https://kolibree.atlassian.net/browse/KLTB002-9495
            // TODO add support for multi-choice enums https://kolibree.atlassian.net/browse/KLTB002-9496
            else -> throw UnsupportedOperationException("${descriptor.feature.type()} is not currently supported!")
        }
    }

    private fun showTextEditDialog(descriptor: FeatureToggleDescriptor<String>) {
        showMaterialDialog(descriptor) { newValue ->
            viewModel.onNewFeatureToggleValue(descriptor, newValue)
        }
    }

    private fun showNumericEditDialog(descriptor: FeatureToggleDescriptor<Long>) {
        showMaterialDialog(descriptor, InputType.TYPE_CLASS_NUMBER) { newValue ->
            try {
                val longValue = newValue.toLong()
                viewModel.onNewFeatureToggleValue(descriptor, longValue)
            } catch (e: NumberFormatException) {
                Timber.e(e)
                execute(ShowOperationFailedAction)
            }
        }
    }

    private fun <T : Any> showMaterialDialog(
        descriptor: FeatureToggleDescriptor<T>,
        inputType: Int = InputType.TYPE_CLASS_TEXT,
        onNewValue: (String) -> Unit
    ) {
        MaterialDialog(this).show {
            lifecycleOwner(this@SecretSettingsActivity)
            title(text = descriptor.displayName)
            positiveButton(R.string.ok)
            negativeButton(R.string.cancel)
            input(
                prefill = descriptor.value.toString(),
                inputType = inputType,
                allowEmpty = false
            ) { _, newValue ->
                onNewValue(newValue.toString())
            }
            resources.getDimension(R.dimen.dot_half).toInt().let {
                getInputField().setPadding(it, 0, it, 0)
            }
        }
    }

    @Suppress("LongMethod")
    private fun showConfirmChangeEnvironmentDialog(environment: Environment) {
        alertDialog(this) {
            lifecycleOwner(this@SecretSettingsActivity)
            title("Change Environment")
            body("Application will logout and delete the local data. You need to login again.")
            cancellable(false) // it should stay not cancellable to restore previous env on cancel
            containedButton {
                title("OK")
                action {
                    viewModel.onChangeEnvironmentConfirmed(environment)
                    dismiss()
                }
            }
            outlinedButton {
                title("Cancel")
                action {
                    viewModel.onChangeEnvironmentCancelled()
                    dismiss()
                }
            }
        }.show()
    }

    private fun showOperationFailedSnackbar() =
        showSnackbar("Unfortunately, operation failed")

    private fun showAppRestartSnackbar() =
        showSnackbar("Kill the app in order to apply changes")

    private fun showSnackbar(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    protected fun disableSecretSettings() {
        secretSettingsManager.disableSecretSettings()
        finish()
    }

    fun onQuickActionClick(@IdRes id: Int) {
        when (id) {
            R.id.secret_settings_force_crash -> CrashLogger.forceCrash()
            R.id.secret_settings_send_exception -> CrashLogger.logException(Exception("This exception is a test"))
            R.id.secret_settings_open_hum_style_ref_sheet ->
                startActivity(ThemeShowcaseActivity::class)
            R.id.secret_settings_open_lottie_playground ->
                startLottiePlaygroundIntent(this)
            R.id.secret_settings_dialog_playground ->
                startDialogsPlaygroundActivity(this)
            R.id.secret_settings_badges_playground ->
                startBadgesPlaygroundActivity(this)
            R.id.secret_settings_chart_playground ->
                startChartPlaygroundIntent(this)
            R.id.secret_settings_speedometer_playground ->
                startSpeedometerPlaygroundIntent(this)
            R.id.secret_settings_force_sync -> forceSyncAndReportResult()
            R.id.secret_settings_disable_secret_settings -> disableSecretSettings()
            R.id.secret_settings_unity_playground -> launchUnityNextGenPlaygroundActivity()
            R.id.secret_settings_shape_spike ->
                startActivity(Intent(this, ShapeSpikeActivity::class.java))
            R.id.secret_settings_select_toothbrsh -> selectToothbrush()
            R.id.secret_settings_fake_brushings -> openFakeBrushingsScreen()
            else -> FailEarly.fail("No handling for view click of id $id")
        }
    }

    private fun openFakeBrushingsScreen() {
        startCreateFakeBrushingIntent(this)
    }

    private fun forceSyncAndReportResult() {
        disposeOnDestroy {
            forceSyncUseCase.force()
                .subscribe(
                    { successOrFailure ->
                        Toast.makeText(
                            this,
                            "Sync completed with $successOrFailure",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    Timber::e
                )
        }
    }

    @SuppressWarnings("LongMethod")
    private fun selectToothbrush() {
        disposeOnDestroy {
            selectToothbrushUseCase
                .selectToothbrush()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    // onSuccess
                    { selectedToothbrush ->
                        Toast.makeText(
                            this,
                            "Toothbrush selected: ${selectedToothbrush.toothbrush().getName()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    // onError
                    {
                        FailEarly.fail("Select toothbrush fail!", it)
                    },
                    // onComplete
                    {
                        Toast.makeText(
                            this,
                            "Toothbrush not selected or not found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
        }
    }
}

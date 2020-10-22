package com.kolibree.android.app.ui.ota;

import static com.kolibree.android.app.ui.ota.OtaUpdateViewStateKt.OTA_ACTION_COMPLETED;
import static com.kolibree.android.app.ui.ota.OtaUpdateViewStateKt.OTA_ACTION_ERROR;
import static com.kolibree.android.app.ui.ota.OtaUpdateViewStateKt.OTA_ACTION_EXIT_CANCEL;
import static com.kolibree.android.app.ui.ota.OtaUpdateViewStateKt.OTA_ACTION_EXIT_SUCCESS;
import static com.kolibree.android.app.ui.ota.OtaUpdateViewStateKt.OTA_ACTION_INSTALLING;
import static com.kolibree.android.app.ui.ota.OtaUpdateViewStateKt.OTA_ACTION_NONE;
import static com.kolibree.android.app.ui.ota.OtaUpdateViewStateKt.OTA_ACTION_REBOOTING;
import static com.kolibree.android.app.ui.ota.OtaUpdateViewStateKt.OTA_CONFIRM_EXIT;
import static com.kolibree.android.app.ui.ota.OtaUpdateViewStateKt.PERCENTAGE_UNDEFINED;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;
import com.kolibree.android.app.ui.activity.BaseActivity;
import com.kolibree.android.app.ui.dialog.KolibreeDialog;
import com.kolibree.android.commons.ToothbrushModel;
import com.kolibree.android.toothbrushupdate.R;
import com.kolibree.android.translationssupport.Translations;
import dagger.android.AndroidInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import javax.inject.Inject;

/** Toothbrush firmware and GRU data OTA update activity */
@Keep
public final class OtaUpdateActivity extends BaseActivity {

  private static final String INTENT_TOOTHBRUSH_MAC = "intentToothbrushMac";
  private static final String INTENT_TOOTHBRUSH_MODEL = "intentToothbrushModel";

  private static final String INTENT_MANDATORY = "intentMandatory";

  @NonNull
  public static Intent createIntent(
      @NonNull Context context,
      @NonNull String toothbrushMac,
      @NonNull ToothbrushModel toothbrushModel,
      boolean isMandatoryUpdate) {

    final Intent intent = new Intent(context, OtaUpdateActivity.class);
    intent.putExtra(INTENT_TOOTHBRUSH_MAC, toothbrushMac);
    intent.putExtra(INTENT_TOOTHBRUSH_MODEL, toothbrushModel);
    intent.putExtra(INTENT_MANDATORY, isMandatoryUpdate);
    return intent;
  }

  Button cancelButton;

  Button actionButton;

  ProgressBar progress;

  TextView message;

  TextView actionText;

  TextView percentText;

  View progressView;

  @Inject OtaUpdateViewModel.Factory viewModelFactory;

  private OtaUpdateViewModel viewModel;

  private Disposable viewModelObservableDisposable;

  private AlertDialog needChargingDialog;

  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(Translations.wrapContext(base));
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_firmware_update);

    initViews();

    viewModel = ViewModelProviders.of(this, viewModelFactory).get(OtaUpdateViewModel.class);
    getLifecycle().addObserver(viewModel);

    if (getIntent().getBooleanExtra(INTENT_MANDATORY, false)) {
      cancelButton.setVisibility(View.INVISIBLE);
    }
  }

  private void initViews() {
    cancelButton = findViewById(R.id.ota_cancel_btn);
    actionButton = findViewById(R.id.ota_action_btn);
    progress = findViewById(R.id.ota_progress);
    message = findViewById(R.id.ota_update_message);
    actionText = findViewById(R.id.ota_action_description);
    percentText = findViewById(R.id.percent);
    progressView = findViewById(R.id.progress_view);

    cancelButton.setOnClickListener(ignore -> onCancelButtonClick());
    actionButton.setOnClickListener(ignore -> onUpgradeButtonClick());
  }

  @Override
  protected void onResume() {
    super.onResume();
    viewModelObservableDisposable =
        viewModel
            .viewStateObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::render, Throwable::printStackTrace);
  }

  @Override
  protected void onPause() {
    viewModelObservableDisposable.dispose();
    super.onPause();
  }

  @Override
  public void onBackPressed() {
    viewModel.onUserClickedBack();
  }

  void onCancelButtonClick() {
    viewModel.onUserClickedCancel();
  }

  void onUpgradeButtonClick() {
    viewModel.onUserClickedActionButton();
  }

  boolean isMandatoryUpdate() {
    return getIntent().getBooleanExtra(INTENT_MANDATORY, false);
  }

  String toothbrushMac() {
    return getIntent().getStringExtra(INTENT_TOOTHBRUSH_MAC);
  }

  ToothbrushModel toothbrushModel() {
    return (ToothbrushModel) getIntent().getSerializableExtra(INTENT_TOOTHBRUSH_MODEL);
  }

  private void render(@NonNull OtaUpdateViewState viewState) {
    renderAction(viewState);

    renderMessage(viewState.message());

    renderButtonVisibility(viewState);

    renderProgressView(viewState);

    renderNeedChargingdialog(viewState);
  }

  private void renderAction(@NonNull OtaUpdateViewState viewState) {
    switch (viewState.otaActionId()) {
      case OTA_ACTION_INSTALLING:
        renderPercent(viewState.otaActionProgressPercentage());
        actionText.setText(R.string.firmware_upgrade_installing);
        break;

      case OTA_ACTION_REBOOTING:
        renderRebooting();
        break;

      case OTA_ACTION_ERROR:
        final String errorMessage = viewState.errorMessage();
        if (errorMessage != null) {
          showErrorDialog(errorMessage);
        }
        break;

      case OTA_ACTION_COMPLETED:
        renderCompleted();
        break;

      case OTA_ACTION_EXIT_SUCCESS:
        setResult(RESULT_OK);
        finish();
        break;

      case OTA_ACTION_EXIT_CANCEL:
        cancelAndFinish();
        break;
      case OTA_CONFIRM_EXIT:
        confirmExit();
        break;
      case OTA_ACTION_NONE:
        break;
    }
  }

  private void confirmExit() {
    KolibreeDialog.create(this, getLifecycle())
        .message(R.string.firmware_upgrade_cancel_dialog_message)
        .negativeButton(R.string.um_yes, () -> viewModel.onUserConfirmedExit())
        .positiveButton(R.string.um_no, () -> viewModel.onUserCanceledExit())
        .show();
  }

  private void renderMessage(@StringRes int messageResId) {
    if (messageResId != 0) {
      message.setText(messageResId);
    } else {
      message.setText(null);
    }
  }

  private void renderButtonVisibility(OtaUpdateViewState viewState) {
    actionButton.setVisibility(viewState.isActionButtonDisplayed() ? View.VISIBLE : View.GONE);
    cancelButton.setVisibility(viewState.isCancelButtonDisplayed() ? View.VISIBLE : View.GONE);
  }

  @SuppressLint("SetTextI18n")
  private void renderPercent(int percents) {
    final boolean indeterminate = percents == PERCENTAGE_UNDEFINED;
    percentText.setVisibility(indeterminate ? View.INVISIBLE : View.VISIBLE);
    percentText.setText(percents + " %");

    progress.setVisibility(indeterminate ? View.VISIBLE : View.GONE);
    progress.setIndeterminate(indeterminate);
    progress.setProgress(percents);
  }

  private void renderRebooting() {
    actionText.setVisibility(View.VISIBLE);
    actionText.setText(R.string.firmware_upgrade_rebooting);

    percentText.setVisibility(View.GONE);
    progress.setIndeterminate(true);
  }

  private void renderCompleted() {
    percentText.setVisibility(View.INVISIBLE);
    actionText.setVisibility(View.INVISIBLE);

    actionButton.setText(android.R.string.ok);

    progress.setVisibility(View.GONE);
  }

  private void renderProgressView(OtaUpdateViewState viewState) {
    progressView.setVisibility(viewState.isProgressVisible() ? View.VISIBLE : View.GONE);
  }

  private void renderNeedChargingdialog(OtaUpdateViewState viewState) {
    if (viewState.showNeedChargingDialog()) {
      showNeedChargingDialog();
    } else {
      hideNeedChargingDialog();
    }
  }

  private void showNeedChargingDialog() {
    if (needChargingDialog == null) {
      needChargingDialog =
          KolibreeDialog.create(this, getLifecycle())
              .message(R.string.ota_blocker_must_be_charging)
              .positiveButton(
                  R.string.ok,
                  new KolibreeDialog.DialogButtonCallback() {
                    @Override
                    public void onClick() {
                      needChargingDialog = null;
                    }
                  })
              .show();
    }
  }

  private void hideNeedChargingDialog() {
    if (needChargingDialog != null) {
      needChargingDialog.dismiss();
      needChargingDialog = null;
    }
  }

  private void showErrorDialog(@NonNull String message) {
    KolibreeDialog.create(this, getLifecycle())
        .title(R.string.error)
        .message(getString(R.string.firmware_upgrade_error_dialog, message))
        .positiveButton(R.string.ok, this::cancelAndFinish)
        .show();
  }

  void cancelAndFinish() {
    setResult(RESULT_CANCELED);
    finish();
  }
}

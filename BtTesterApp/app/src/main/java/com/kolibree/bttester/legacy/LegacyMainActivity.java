/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.bttester.legacy;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import butterknife.Action;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.ViewCollections;
import com.kolibree.android.app.ui.activity.KolibreeServiceActivity;
import com.kolibree.android.sdk.scan.ToothbrushScanResult;
import com.kolibree.bttester.R;
import com.kolibree.bttester.legacy.PairDialogFragment.OnDeviceSelectedListener;
import com.kolibree.bttester.tester.ConnectionTester;
import com.kolibree.bttester.tester.CycleConnectionTester;
import com.kolibree.bttester.tester.SingleConnectionTester;
import com.kolibree.pairing.assistant.PairingAssistant;
import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import timber.log.Timber;

public class LegacyMainActivity extends KolibreeServiceActivity
    implements HasAndroidInjector, OnDeviceSelectedListener {

  private static final String EXTRA_SCAN_RESULT = "extra_scan_result";
  private static final int MY_PERMISSIONS_REQUEST_WRITE = 90;
  final CompositeDisposable disposables = new CompositeDisposable();

  @BindView(R.id.scan_button)
  Button pairButton;

  @BindView(R.id.selected_device)
  TextView selectedDeviceTextView;

  @BindView(R.id.cycle_destroy_btn)
  Button startCycleButton;

  @BindView(R.id.nb_of_seconds)
  TextView nbOfSecondsEditText;

  @BindView(R.id.explanation)
  TextView bottomTextView;

  @BindViews({R.id.cycle_destroy_btn, R.id.nb_of_seconds})
  List<View> deviceDependantViews;

  @Inject PairingAssistant pairingAssistant;
  private ToothbrushScanResult scanResult;
  private Disposable testerDisposable;
  /*
  If we don't keep a strong reference to our ConnectionTester, it might be garbage collected and
  we will not be notified by ListenerPool
   */
  private ConnectionTester connectionTester;

  @Inject DispatchingAndroidInjector<Object> fragmentInjector;

  @Override
  public AndroidInjector<Object> androidInjector() {
    return fragmentInjector;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_legacy_main);
    ButterKnife.bind(this);

    if (savedInstanceState != null) {
      scanResult = savedInstanceState.getParcelable(EXTRA_SCAN_RESULT);
    } else {
      checkWritePermissions();
    }
  }

  private void checkWritePermissions() {
    if (ContextCompat.checkSelfPermission(this, permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {

      // Should we show an explanation?
      if (ActivityCompat.shouldShowRequestPermissionRationale(
          this, permission.WRITE_EXTERNAL_STORAGE)) {

        // Show an explanation to the user *asynchronously* -- don't block
        // this thread waiting for the user's response! After the user
        // sees the explanation, try again to request the permission.

      } else {

        // No explanation needed, we can request the permission.

        ActivityCompat.requestPermissions(
            this,
            new String[] {permission.WRITE_EXTERNAL_STORAGE, permission.READ_EXTERNAL_STORAGE},
            MY_PERMISSIONS_REQUEST_WRITE);
      }
    }
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, String permissions[], int[] grantResults) {
    switch (requestCode) {
      case MY_PERMISSIONS_REQUEST_WRITE:
        {
          // If request is cancelled, the result arrays are empty.
          if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {}
        }
    }
  }

  @Override
  protected void onStart() {
    super.onStart();

    if (scanResult != null) {
      onDeviceSelected(scanResult);
    } else {
      ViewCollections.run(deviceDependantViews, DISABLED);
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    if (scanResult != null) {
      outState.putParcelable(EXTRA_SCAN_RESULT, scanResult);
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    disposables.dispose();
  }

  @OnClick(R.id.scan_button)
  void onPairButtonClicked() {
    PairDialogFragment.showIfNotPresent(getSupportFragmentManager());
  }

  @OnClick(R.id.single_connection)
  void onSingleConnectionClicked() {
    if (!checkTestPreconditions()) {
      return;
    }

    if (testerDisposable != null && !testerDisposable.isDisposed()) {
      testerDisposable.dispose();
    }

    SingleConnectionTester singleConnectionTester = SingleConnectionTester.Companion.create();

    testerDisposable =
        singleConnectionTester
            .testFor(getService(), pairingAssistant, scanResult)
            .subscribe(this::appentToResultsText, Timber::e);

    disposables.add(testerDisposable);

    connectionTester = singleConnectionTester;
  }

  @OnClick({R.id.cycle_destroy_btn, R.id.cycle_reuse_btn})
  void onStartCycleClicked(View view) {
    if (TextUtils.isEmpty(nbOfSecondsEditText.getText())) {
      Toast.makeText(this, "Enter nb of seconds", Toast.LENGTH_SHORT).show();
      return;
    }

    hideKeyboard();

    int nbOfSeconds = Integer.valueOf(nbOfSecondsEditText.getText().toString());
    startTimeCycle(nbOfSeconds, view.getId() == R.id.cycle_reuse_btn);
  }

  private void hideKeyboard() {
    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
        .hideSoftInputFromWindow(nbOfSecondsEditText.getWindowToken(), 0);

    nbOfSecondsEditText.clearFocus();
  }

  private void startTimeCycle(int nbOfSeconds, boolean reuseConnections) {
    if (!checkTestPreconditions()) {
      return;
    }

    if (testerDisposable != null && !testerDisposable.isDisposed()) {
      testerDisposable.dispose();
    }

    CycleConnectionTester cycleConnectionTester = CycleConnectionTester.create(reuseConnections);
    testerDisposable =
        cycleConnectionTester
            .testFor(getService(), scanResult, nbOfSeconds, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe(
                ignore -> getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON))
            .doOnSubscribe(ignore -> startCycleButton.setEnabled(false))
            .onTerminateDetach()
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally(() -> startCycleButton.setEnabled(true))
            .doOnTerminate(
                () -> getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON))
            .subscribe(this::setResultsText, Throwable::printStackTrace);

    disposables.add(testerDisposable);

    connectionTester = cycleConnectionTester;
  }

  private boolean checkTestPreconditions() {
    if (getService() == null) {
      Toast.makeText(
              this,
              "Connection to KolibreeService not established. Can't proceed.",
              Toast.LENGTH_LONG)
          .show();
      return false;
    }

    if (scanResult == null) {
      Toast.makeText(this, "Please select a device", Toast.LENGTH_LONG).show();
      return false;
    }
    return true;
  }

  private void setResultsText(String testReport) {
    bottomTextView.setText(testReport);
  }

  private void appentToResultsText(String newTextLine) {
    bottomTextView.append("\n" + newTextLine);
  }

  @Override
  public void onDeviceSelected(ToothbrushScanResult scanResult) {
    this.scanResult = scanResult;

    ViewCollections.run(deviceDependantViews, ENABLED);

    selectedDeviceTextView.setText(
        "Selected device: " + scanResult.getName() + " (" + scanResult.getMac() + ")");
  }

  @Override
  public void onDialogCanceled() {}

  static final Action<View> ENABLED = (view, index) -> view.setEnabled(true);
  static final Action<View> DISABLED = (view, index) -> view.setEnabled(false);
}

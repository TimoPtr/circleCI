package cn.colgate.colgateconnect.base;

import static com.kolibree.account.logout.LogoutEnforcerKt.EXTRA_FORCED_LOGOUT;

import android.Manifest.permission;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import butterknife.ButterKnife;
import cn.colgate.colgateconnect.R;
import cn.colgate.colgateconnect.demo.DataStore;
import cn.colgate.colgateconnect.login.LoginActivity;
import com.kolibree.account.AccountFacade;
import com.kolibree.account.logout.ForceLogoutReason;
import com.kolibree.account.logout.IntentAfterForcedLogout;
import com.kolibree.android.app.ui.activity.KolibreeServiceActivity;
import com.kolibree.android.location.LocationStatus;
import com.kolibree.android.sdk.util.IBluetoothUtils;
import com.umeng.message.PushAgent;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import javax.inject.Inject;

/** Base class for all the activity, that contains all the common utils methods */
public abstract class SdkDemoBaseActivity extends KolibreeServiceActivity {

  private static final int PERMISSION_REQUEST_LOCATION = 1;
  private static final int LOCATION_REQUEST_CODE = 999;
  // to avoid memory leak
  protected final CompositeDisposable disposables = new CompositeDisposable();
  // BT utils from the toothbrush module, it's not required. It only contains helpers
  @Inject protected IBluetoothUtils iBluetoothUtils;

  @Inject protected LocationStatus locationStatus;

  @Inject protected AccountFacade accountFacade;

  @Inject DataStore dataStore;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    turnOnBluetooth();
    PushAgent.getInstance(this).onAppStart();

    listenToShouldLogout();
  }

  private void listenToShouldLogout() {
    disposables.add(
        accountFacade
            .shouldLogout()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onForceLogout, Throwable::printStackTrace));
  }

  @CallSuper
  protected void onForceLogout(ForceLogoutReason forceLogoutReason) {
    dataStore.clean();

    IntentAfterForcedLogout intent = IntentAfterForcedLogout.create(this, LoginActivity.class);
    intent.putExtra(EXTRA_FORCED_LOGOUT, forceLogoutReason);
    startActivity(intent);
    finish();
  }

  @Override
  public void setContentView(@LayoutRes int layoutResID) {
    super.setContentView(layoutResID);
    ButterKnife.bind(this); // include Butterknife in all activities
    hideProgress();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    // free the memory
    disposables.dispose();
  }

  /** Display the spinner */
  protected void showProgress() {
    View v = findViewById(R.id.loading);
    if (v != null) {
      v.setVisibility(View.VISIBLE);
    }
  }

  /** hide the spinner */
  protected void hideProgress() {
    View v = findViewById(R.id.loading);
    if (v != null) {
      v.setVisibility(View.GONE);
    }
  }

  protected void displayBackButtonToolbar() {
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // handle arrow click here
    if (item.getItemId() == android.R.id.home) {
      finish(); // close this activity and return to preview activity (if there is any)
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onEnableLocationActionNeeded() {
    super.onEnableLocationActionNeeded();

    showLocationNeededDialog();
  }

  protected final void showLocationNeededDialog() {
    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("This app needs location access");
    builder.setMessage("Please grant location access so this app can detect toothbrushes.");
    builder.setPositiveButton(android.R.string.ok, null);
    builder.setOnDismissListener(
        dialog ->
            requestPermissions(
                new String[] {
                  permission.ACCESS_COARSE_LOCATION,
                  permission.READ_EXTERNAL_STORAGE,
                  permission.WRITE_EXTERNAL_STORAGE,
                  permission.ACCESS_FINE_LOCATION
                },
                PERMISSION_REQUEST_LOCATION));
    builder.show();
  }

  protected void startLocationSettingsActivity() {
    startActivityForResult(
        new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), LOCATION_REQUEST_CODE);
  }

  protected void displayError(@StringRes int msgID, Throwable throwable) {
    Toast.makeText(this, msgID, Toast.LENGTH_LONG).show();
    throwable.printStackTrace();
    hideProgress();
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
    switch (requestCode) {
      case PERMISSION_REQUEST_LOCATION:
        {
          if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("ScanActivity", "coarse location permission granted");
            if (!locationStatus.isReadyToScan()) {
              startLocationSettingsActivity();
            }
          } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Functionality limited");
            builder.setMessage(
                "Since location access has not been granted, this app will not be able to discover toothbrushes.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(dialog -> dialog.dismiss());
            builder.show();
          }
        }
    }
  }

  protected void turnOffBluetooth() {
    iBluetoothUtils.enableBluetooth(false);
  }

  protected void turnOnBluetooth() {
    iBluetoothUtils.enableBluetooth(true);
  }
}

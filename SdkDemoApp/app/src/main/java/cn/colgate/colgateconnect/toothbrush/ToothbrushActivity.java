package cn.colgate.colgateconnect.toothbrush;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import butterknife.BindView;
import butterknife.OnClick;
import cn.colgate.colgateconnect.R;
import cn.colgate.colgateconnect.base.SdkDemoBaseActivity;
import cn.colgate.colgateconnect.demo.AccountInfo;
import cn.colgate.colgateconnect.orphanbrushings.ProfileListDialogFragment;
import com.kolibree.android.accountinternal.profile.models.IProfile;
import com.kolibree.android.extensions.DisposableUtils;
import com.kolibree.android.sdk.connection.toothbrush.dsp.DspState;
import com.kolibree.android.sdk.version.DspVersion;
import com.kolibree.android.sdk.wrapper.ToothbrushFacade;
import com.kolibree.android.tracker.Analytics;
import com.kolibree.android.tracker.AnalyticsEvent;
import com.kolibree.pairing.assistant.PairingAssistant;
import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import javax.inject.Inject;

/** Display the settings of the TB */
public class ToothbrushActivity extends SdkDemoBaseActivity
    implements HasAndroidInjector, ProfileListDialogFragment.ProfileSelectorCallback {

  private final CompositeDisposable disposables = new CompositeDisposable();
  // pairing module
  @Inject PairingAssistant pairingAssistant;
  @Inject AccountInfo accountInfo;
  @Inject DispatchingAndroidInjector<Object> fragmentInjector;

  @BindView(R.id.toothbrush_name)
  TextView toothbrush_name;

  @BindView(R.id.toothbrush_battery_level)
  TextView toothbrush_battery_level;

  @BindView(R.id.toothbrush_user_name)
  TextView toothbrushOwnerName;

  @BindView(R.id.toothbrush_serial)
  TextView toothbrush_serial;

  @BindView(R.id.toothbrush_mac)
  TextView toothbrush_mac;

  @BindView(R.id.toothbrush_hardware)
  TextView toothbrush_hardware;

  @BindView(R.id.toothbrush_software)
  TextView toothbrush_software;

  @BindView(R.id.toothbrush_dsp)
  TextView dspVersionsText;

  // paired toothbrush
  private ToothbrushFacade toothbrush;

  public static void start(Context context) {
    Intent starter = new Intent(context, ToothbrushActivity.class);
    context.startActivity(starter);
  }

  @Override
  public AndroidInjector<Object> androidInjector() {
    return fragmentInjector;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_toothbrush);
  }

  @Override
  public void onStart() {
    super.onStart();
    turnOnBluetooth();
    displayBackButtonToolbar();

    toothbrush = accountInfo.getPairingSession().toothbrush();

    // The device's parameters are not available in bootloader mode
    if (!toothbrush.isRunningBootloader()) {
      displayBatteryState(toothbrush);
    }
    displayToothbrushData();

    Analytics.send(new AnalyticsEvent("ToothbrushActivity_Start"));
  }

  @OnClick(R.id.toothbrush_user_container)
  public void onToothbrushOwnerclicked() {
    if (accountInfo.getProfiles().size() > 1) {
      ProfileListDialogFragment.showWithSharedText(getSupportFragmentManager());
    } else {
      Toast.makeText(this, "This account only has 1 profile", Toast.LENGTH_LONG).show();
    }
  }

  @OnClick(R.id.btn_forget)
  public void forgetToothbrush() {
    disposables.add(
        pairingAssistant
            .unpair(toothbrush.getMac())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::removePairingAndClose, Throwable::printStackTrace));
  }

  private void removePairingAndClose() {
    accountInfo.setPairingSession(null);
    finish();
  }

  /** Display the data of the TB on the ui */
  private void displayToothbrushData() {
    toothbrush_serial.setText(toothbrush.getSerialNumber());
    toothbrush_hardware.setText(toothbrush.getHardwareVersion().toString());
    toothbrush_mac.setText(toothbrush.getMac());
    toothbrush_name.setText(toothbrush.getName());
    toothbrush_software.setText(toothbrush.getFirmwareVersion().toString());

    loadToothbrushUser();
    loadDspVersions();
  }

  private void loadToothbrushUser() {
    disposables.add(
        toothbrush
            .isSharedModeEnabled()
            .subscribeOn(Schedulers.io())
            .flatMap(
                isMultiModeEnabled -> {
                  if (isMultiModeEnabled) {
                    return Single.just(getString(R.string.settings_shared_toothbrush));
                  } else {
                    return toothbrush.getProfileId().map(this::loadProfileName);
                  }
                })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                ownerName -> toothbrushOwnerName.setText(ownerName), Throwable::printStackTrace));
  }

  private void loadDspVersions() {
    DisposableUtils.addSafely(
        disposables,
        accountInfo
            .getPairingSession()
            .connection()
            .toothbrush()
            .dspState()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::showDspVersions, ignore -> showNoDsp()));
  }

  private void showDspVersions(@NonNull DspState state) {
    dspVersionsText.setText(
        String.format(
            "DSP firmware version: %s\nFlash file type: %s\nFlash file version: %s",
            state.getFirmwareVersion().toString().replace(DspVersion.NULL.toString(), "None"),
            state.getFlashFileType().name().replace("_", ""),
            state.getFlashFileVersion().toString().replace(DspVersion.NULL.toString(), "None")));
  }

  private void showNoDsp() {
    dspVersionsText.setText(R.string.no_dsp);
  }

  @NonNull
  private String loadProfileName(Long ownerId) {
    for (IProfile accountInfoProfile : accountInfo.getProfiles()) {
      if (accountInfoProfile.getId() == ownerId) {
        return accountInfoProfile.getFirstName();
      }
    }

    return "Unknown owner (" + ownerId + ")";
  }

  /**
   * Get the battery state and update the view automatically
   *
   * @param toothbrushFacade Toothbrush
   */
  @SuppressLint("SetTextI18n")
  private void displayBatteryState(ToothbrushFacade toothbrushFacade) {
    disposables.add(
        toothbrushFacade
            .getBatteryLevel()
            .subscribeOn(Schedulers.io())
            .retry(3)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                level -> toothbrush_battery_level.setText(level + "%"),
                Throwable::printStackTrace));
  }

  @Override
  public void onCancel(DialogInterface dialog) {
    // do nothing
  }

  @Override
  public void onProfileSelected(long profileId) {
    Completable actionCompletable;
    String ownerName;
    if (profileId == ProfileListDialogFragment.ID_SHARED_TOOTHBRUSH) {
      actionCompletable = toothbrush.enableSharedMode();
      ownerName = "Shared Toothbrush";
    } else {
      actionCompletable = toothbrush.setProfileId(profileId);
      ownerName = loadProfileName(profileId);
    }

    disposables.add(
        actionCompletable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                () -> toothbrushOwnerName.setText(ownerName),
                throwable -> {
                  Toast.makeText(this, "Error setting profileId", Toast.LENGTH_LONG).show();
                  throwable.printStackTrace();
                }));
  }
}

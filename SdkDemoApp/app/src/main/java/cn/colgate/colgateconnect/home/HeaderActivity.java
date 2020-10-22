package cn.colgate.colgateconnect.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.StringRes;
import butterknife.BindView;
import butterknife.OnClick;
import cn.colgate.colgateconnect.R;
import cn.colgate.colgateconnect.base.SdkDemoBaseActivity;
import cn.colgate.colgateconnect.demo.AccountInfo;
import cn.colgate.colgateconnect.demo.DataStore;
import cn.colgate.colgateconnect.home.fragments.HomeFragmentPagerAdapter;
import cn.colgate.colgateconnect.login.LoginActivity;
import cn.colgate.colgateconnect.profile.ProfilesListActivity;
import cn.colgate.colgateconnect.scan.ScanActivity;
import cn.colgate.colgateconnect.toothbrush.ToothbrushActivity;
import cn.colgate.colgateconnect.utils.ApiError;
import cn.colgate.colgateconnect.utils.ImageUtils;
import com.kolibree.account.AccountFacade;
import com.kolibree.account.ProfileFacade;
import com.kolibree.account.profile.ProfileExtension;
import com.kolibree.account.utils.ToothbrushesForProfileUseCase;
import com.kolibree.android.accountinternal.profile.models.IProfile;
import com.kolibree.android.jaws.Kolibree3DModel;
import com.kolibree.android.jaws.MemoryManager;
import com.kolibree.android.sdk.connection.CheckConnectionPrerequisitesUseCase;
import com.kolibree.android.sdk.connection.KLTBConnection;
import com.kolibree.android.sdk.connection.state.ConnectionStateListener;
import com.kolibree.android.sdk.connection.state.KLTBConnectionState;
import com.kolibree.pairing.assistant.PairingAssistant;
import com.kolibree.sdkws.brushing.wrapper.BrushingFacade;
import com.kolibree.sdkws.core.AvatarCache;
import dagger.android.AndroidInjection;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import timber.log.Timber;

/**
 * Everything that manage the header, such as the user info, the toothbrush status and the logout I
 * created 2 activities in order to avoid having all the code in 1 activity only.
 */
public abstract class HeaderActivity extends SdkDemoBaseActivity
    implements ConnectionStateListener {

  private static final int RESULT_LOAD_IMG = 300;
  protected HomeFragmentPagerAdapter adapter;
  // profile module
  @Inject AccountFacade accountFacade;
  @Inject ProfileFacade profileFacade;
  @Inject BrushingFacade brushingFacade;
  // jaws module
  @Inject MemoryManager memoryManager;
  // pairing module
  @Inject PairingAssistant pairingAssistant;
  // Your DataStore, to store locally the user to connect him again at the next session
  @Inject DataStore dataStore;
  // your profile data container, where you will store the data of the user for this session
  @Inject AccountInfo accountInfo;
  // get all toothbrush connections of current profile
  @Inject ToothbrushesForProfileUseCase toothbrushesForProfileUseCase;
  // Check whether all connection prerequisites are met
  @Inject CheckConnectionPrerequisitesUseCase checkConnectionPrerequisitesUseCase;
  @Inject AvatarCache avatarCache;

  @BindView(R.id.tvName)
  TextView tvName;

  @BindView(R.id.ivToothbrush)
  ImageView ivToothbrush;

  @BindView(R.id.ivBoy)
  ImageView ivBoy;

  private KLTBConnection currentConnection;
  /** Some image utils */
  private ImageUtils imageUtils;

  private Flowable<IProfile> activeProfileFlowable;

  // just an object to encapsulate. Not required if you are familiar with ViewModel

  @Override
  public void onCreate(Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
    imageUtils = new ImageUtils(this);

    activeProfileFlowable = profileFacade.activeProfileFlowable().share();

    loadInfoOnActiveProfileChanges();
  }

  private void loadInfoOnActiveProfileChanges() {
    disposables.add(
        activeProfileFlowable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext(newProfile -> accountInfo.setCurrentProfile(newProfile))
            .subscribe(ignoredProfile -> loadProfile(), Throwable::printStackTrace));
  }

  protected void init() {
    preload3DModels();
    listenToToothbrushConnections();
  }

  private void listenToToothbrushConnections() {
    disposables.add(
        toothbrushesForProfileUseCase
            .currentProfileToothbrushesOnceAndStream()
            .subscribeOn(Schedulers.io())
            .distinctUntilChanged()
            .onTerminateDetach()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::handleConnections, Timber::e));
  }

  private void handleConnections(List<KLTBConnection> connections) {
    if (!connections.isEmpty() && currentConnection == null) {
      saveConnection(connections.get(0));
      savePairingSession();
      updateConnectionStates();
    }
  }

  private void saveConnection(KLTBConnection connection) {
    currentConnection = connection;
    currentConnection.state().register(this);
  }

  private void savePairingSession() {
    if (currentConnection == null) {
      return;
    }
    accountInfo.setPairingSession(pairingAssistant.createPairingSession(currentConnection));
  }

  private void updateConnectionStates() {
    if (currentConnection == null) {
      setToothbrushState(R.drawable.ic_toothbrush_dark);
      return;
    }

    switch (currentConnection.state().getCurrent()) {
      case ACTIVE:
        setToothbrushState(R.drawable.ic_toothbrush_connected_dark);
        break;
      case TERMINATED:
        setToothbrushState(R.drawable.ic_toothbrush_disconnected_dark);
        break;
      default:
        setToothbrushState(R.drawable.ic_toothbrush_connecting_dark);
        break;
    }
  }

  private void setToothbrushState(int resId) {
    ivToothbrush.setImageResource(resId);
  }

  @Override
  public void onConnectionStateChanged(
      @NotNull KLTBConnection connection, @NotNull KLTBConnectionState newState) {
    updateConnectionStates();
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (currentConnection != null) {
      currentConnection.state().unregister(this);
    }
  }

  /** Display the info related to the profile on the screen */
  protected void loadProfile() {
    if (accountInfo.getCurrentProfile() != null) {
      IProfile profile = accountInfo.getCurrentProfile();
      tvName.setText(profile.getFirstName());
      ivBoy.setImageResource(R.drawable.boy);
      String avatarUrl = avatarCache.getAvatarUrl(profile);
      imageUtils.loadAvatar(avatarUrl, ivBoy);
      synchronizeBrushings();
      getProfileslist();
    } else {

      // session expired or weird state
      logout();
    }
  }

  private void synchronizeBrushings() {
    disposables.add(
        brushingFacade
            .synchronizeBrushing(accountInfo.getCurrentProfile().getId())
            .flatMap(it -> brushingFacade.getBrushings(accountInfo.getCurrentProfile().getId()))
            .subscribeOn(Schedulers.io())
            .subscribe(
                brushings -> Timber.i("the user has  : " + brushings.size() + " brushings"),
                ApiError::displayErrorMessage));
  }

  private void getProfileslist() {
    disposables.add(
        profileFacade
            .getProfilesList()
            .subscribeOn(Schedulers.io())
            .subscribe(
                profiles -> {
                  accountInfo.setProfiles(profiles);
                  Timber.e("Fetched profile list : %s", profiles.toString());
                },
                ApiError::displayErrorMessage));
  }

  @Override
  public void onStart() {
    super.onStart();
    if (currentConnection != null) {
      currentConnection.state().register(this);
    }
  }

  /**
   * If the user has a TB associated to his account, and connected, it will displayed the detail of
   * the TB, otherwise scan to discover the TB nearby.
   */
  @OnClick(R.id.ivToothbrush)
  void clickToothbrush() {
    if (accountInfo.getPairingSession() != null) {
      ToothbrushActivity.start(this);
    } else {
      onStartPairing();
    }
  }

  /*
  This method checks whether all pairing preconditions are met.
  The flow implemented in this method is the most basic to have all the preconditions met. In a real
  application, the reason of this should be explained to the end user
   */
  private void onStartPairing() {
    switch (checkConnectionPrerequisitesUseCase.checkConnectionPrerequisites()) {
      case BluetoothDisabled: // Need for bluetooth to be enabled
        startActivity(new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS));
        break;

      case LocationServiceDisabled: // Location service is disabled (needed for scanning)
        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        break;

      case LocationPermissionNotGranted: // Since Android 6 location permission must be granted
        showLocationNeededDialog();
        break;

      case ConnectionAllowed: // All conditions are met, start pairing
        ScanActivity.start(this);
        break;
    }
  }

  /**
   * Preload the model of the JAWS using the memory manager from the Jaws 3D module in order to
   * improve the loading performance and avoid any latencies
   */
  private void preload3DModels() {
    disposables.add(
        memoryManager
            .preloadFromAssets(Kolibree3DModel.UPPER_JAW)
            .subscribeOn(Schedulers.computation())
            .andThen(memoryManager.preloadFromAssets(Kolibree3DModel.LOWER_JAW))
            .andThen(memoryManager.preloadFromAssets(Kolibree3DModel.TOOTHBRUSH))
            .subscribe(() -> {}, Throwable::printStackTrace));
  }

  private void uploadPicture(String picturePath) {
    showProgress();
    disposables.add(
        profileFacade
            .changeProfilePicture(accountInfo.getCurrentProfile(), picturePath)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                profile -> {
                  accountInfo.setCurrentProfile(profile);
                  dataStore.storeProfile(profile);
                  String avatarUrl =
                      ProfileExtension.getAvatarUrl(profile, getApplicationContext());
                  imageUtils.loadAvatar(avatarUrl, ivBoy);
                  hideProgress();
                },
                t -> displayError(R.string.error_try_again, t)));
  }

  protected void displayError(@StringRes int msgID, Throwable throwable) {
    super.displayError(msgID, throwable);
    ivToothbrush.setImageResource(R.drawable.ic_toothbrush_disconnected_dark);
  }

  @OnClick(R.id.ivLogout)
  public void logout() {
    disposables.add(
        accountFacade
            .logout()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::logoutSuccess, Throwable::printStackTrace));
  }

  private void logoutSuccess() {
    dataStore.clean();
    LoginActivity.start(this);
    finish();
  }

  /** Open the gallery to pick a picture */
  @OnClick(R.id.ivBoy)
  void pickAvatar() {
    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
    photoPickerIntent.setType("image/*");
    startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
  }

  @OnClick(R.id.tvName)
  void openProfilesList() {
    ProfilesListActivity.start(this);
  }

  /** Get the result when an image is picked */
  @Override
  protected void onActivityResult(int reqCode, int resultCode, Intent data) {
    super.onActivityResult(reqCode, resultCode, data);

    if (resultCode == RESULT_OK && data != null) {
      final Uri imageUri = data.getData();
      if (imageUri != null) {
        uploadPicture(imageUtils.getImagePath(imageUri));
      }
    }
  }
}

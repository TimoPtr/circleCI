package cn.colgate.colgateconnect.home;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener;
import butterknife.BindView;
import cn.colgate.colgateconnect.R;
import cn.colgate.colgateconnect.home.fragments.HomeFragmentPagerAdapter;
import cn.colgate.colgateconnect.update.OtaCheckerManager;
import cn.colgate.colgateconnect.update.OtaView;
import cn.colgate.colgateconnect.wxapi.WXApiManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kolibree.android.app.ui.ota.OtaUpdateActivity;
import com.kolibree.android.commons.ToothbrushModel;
import com.kolibree.android.offlinebrushings.OfflineBrushingsRetrieverViewModel;
import com.kolibree.android.offlinebrushings.OfflineBrushingsRetrieverViewState;
import com.kolibree.android.toothbrushupdate.OtaChecker;
import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import javax.inject.Inject;

/**
 * Demo app to show the usage of the SDK only. Homepage, display the dashboard of the logged user,
 * with the total number of brushings, a couple of last week data and the last brushing session, if
 * any.
 */
public class MainActivity extends HeaderActivity implements HasAndroidInjector, OtaView {

  private static final int OTA_UPDATE_FLOW = 402;
  public static final int REQUEST_CODE_GAME = 1003;

  @Nullable public Boolean assetBundleActivityLaunched = null;

  @BindView(R.id.home_viewpager)
  ViewPager pager; // Home pager

  @BindView(R.id.bottom_navigation)
  BottomNavigationView bottomNavigationView;

  @Inject DispatchingAndroidInjector<Object> dispatchingAndroidInjector;

  // offline and orphan brushing module
  @Inject OfflineBrushingsRetrieverViewModel.Factory offlineRetrieverViewModelFactory;

  // toothbrush update module
  @Inject OtaChecker otaChecker;
  private OtaCheckerManager otaCheckerManager;

  private Dialog dialog = null;

  public static void start(Context context) {
    Intent starter = new Intent(context, MainActivity.class);
    context.startActivity(starter);
  }

  @Override
  public AndroidInjector<Object> androidInjector() {
    return dispatchingAndroidInjector;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    init();
    initOfflineBrushingsRetrieverViewModel();
    setupBottomNavigationView();
    initDialog();
    otaCheckerManager = new OtaCheckerManager(otaChecker, this);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
  }

  @Override
  protected void onActivityResult(int reqCode, int resultCode, Intent data) {
    if (reqCode == REQUEST_CODE_GAME && resultCode == RESULT_OK) {
      bottomNavigationView.setSelectedItemId(R.id.navigation_tab_3);
    } else super.onActivityResult(reqCode, resultCode, data);
  }

  // ------------------   OFFLINE BRUSHINGS MODULE    -------------------------------------

  // setup offline brushing and subscribe to get notifed when a brushing is sync
  private void initOfflineBrushingsRetrieverViewModel() {
    // 2. Instantiate the ViewModel
    OfflineBrushingsRetrieverViewModel offlineBrushingsRetrieverViewModel =
        ViewModelProviders.of(this, offlineRetrieverViewModelFactory)
            .get(OfflineBrushingsRetrieverViewModel.class);

    // 4. Subscribe to OfflineBrushingsRetrieverViewState
    disposables.add(
        offlineBrushingsRetrieverViewModel
            .viewStateObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::renderOfflineBrushingsRetrieverViewState, Throwable::printStackTrace));
  }

  // 5. Update the UI
  private void renderOfflineBrushingsRetrieverViewState(
      OfflineBrushingsRetrieverViewState offlineRetrieverViewState) {
    if (offlineRetrieverViewState.haveRecordsBeenRetrievedForCurrentProfile()) {
      showOfflineRecordsSynchedDialog(offlineRetrieverViewState.getRecordsRetrieved());
    }
  }

  /**
   * Alert the user when a brushing is sync
   *
   * @param recordCount total number of brushing synced
   */
  private void showOfflineRecordsSynchedDialog(int recordCount) {

    final Dialog dialog = new Dialog(this);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setCancelable(false);
    dialog.setContentView(R.layout.dialog_offline_brushing);

    TextView text = dialog.findViewById(R.id.tvTitle);
    text.setText(getOfflineSyncDialogMessage(recordCount));
    dialog.show();
    dialog.findViewById(R.id.btn).setOnClickListener(v -> dialog.dismiss());
  }

  @NonNull
  private String getOfflineSyncDialogMessage(int recordCount) {
    String dialogMessage;
    if (recordCount == 1) {
      dialogMessage = getString(R.string.popup_brushing_record_message_singular);
    } else {
      dialogMessage = getString(R.string.popup_brushing_record_message_multiple, recordCount);
    }
    return dialogMessage;
  }

  // ------------------    BOTTOM NAV BAR     -------------------------------------

  /** Setup bottom nav bar */
  private void setupBottomNavigationView() {
    adapter = new HomeFragmentPagerAdapter(getSupportFragmentManager());
    pager.setAdapter(adapter);
    bottomNavigationView.setSelectedItemId(R.id.navigation_tab_1);
    bottomNavigationView.setOnNavigationItemSelectedListener(this::selectAppropriatePage);

    pager.addOnPageChangeListener(
        new SimpleOnPageChangeListener() {
          @Override
          public void onPageSelected(int position) {
            bottomNavigationView.setSelectedItemId(pagePositionToId(position));
          }
        });
  }

  private boolean selectAppropriatePage(MenuItem item) {
    setSelected(menuItemToPosition(item));
    return true;
  }

  private int pagePositionToId(int position) {
    switch (position) {
      case 0:
        return R.id.navigation_tab_1;
      case 1:
        return R.id.navigation_tab_2;
      case 2:
        return R.id.navigation_tab_3;
      case 3:
        return R.id.navigation_tab_4;
      default:
        throw new IllegalArgumentException("Invalid position: " + position);
    }
  }

  private int menuItemToPosition(MenuItem item) {
    int id = item.getItemId();
    switch (id) {
      case R.id.navigation_tab_1:
        return 0;
      case R.id.navigation_tab_2:
        return 1;
      case R.id.navigation_tab_3:
        return 2;
      case R.id.navigation_tab_4:
        return 3;
      default:
        throw new IllegalArgumentException("Invalid item: " + item.getItemId());
    }
  }

  public void setSelected(int index) {
    pager.setCurrentItem(index, false);
  }

  // ------------------    TOOTHBRUSH UPDATE UI     -------------------------------------

  private void initDialog() {
    dialog = new Dialog(this);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setCancelable(false);
  }

  /**
   * Display popup to inform the user there is a mandatory update to do
   *
   * @param mac mac addr of the toothbrush to update
   */
  @Override
  public void onMandatoryUpdateNeeded(String mac, ToothbrushModel model) {
    if (!isFinishing() && dialog != null) {
      dialog.setContentView(R.layout.dialog_force_update);
      dialog
          .findViewById(R.id.btn)
          .setOnClickListener(
              v -> {
                dialog.dismiss();
                displayUpdateView(mac, model, true);
              });
      dialog.show();
    }
  }

  @Override
  public void requestEnableInternet() {
    Toast.makeText(this, R.string.enable_internet, Toast.LENGTH_LONG).show();
  }

  /**
   * Open toothbrush update UI activity
   *
   * @param mac mac addr of the toothbrush to update
   * @param isMandatoryUpdate boolean to check if it's a mandatory update or not
   */
  private void displayUpdateView(String mac, ToothbrushModel model, Boolean isMandatoryUpdate) {
    Intent intent = OtaUpdateActivity.createIntent(this, mac, model, isMandatoryUpdate);
    startActivityForResult(intent, OTA_UPDATE_FLOW);
    // you should check onActivityResult to know if it has completed correctly or not
  }

  /**
   * Display popup to inform the user there is a new update available
   *
   * @param mac mac addr of the toothbrush to update
   */
  @Override
  public void updateAvailable(String mac, ToothbrushModel model) {

    if (!isFinishing() && dialog != null) {
      dialog.setContentView(R.layout.dialog_update_available);
      dialog.show();
      dialog
          .findViewById(R.id.btn)
          .setOnClickListener(
              v -> {
                dialog.dismiss();
                displayUpdateView(mac, model, false);
              });

      dialog.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());
    }
  }

  @Override
  public void onStart() {
    super.onStart();
    otaCheckerManager.onStart(); // verify if there is a new update
  }

  @Override
  public void onStop() {
    super.onStop();
    otaCheckerManager.onStop(); // stop to check, the app/activity is in background
  }

  public void onWeChatLogin() {
    WXApiManager.getInstance().requestWeChatCode(this);
  }
}

package cn.colgate.colgateconnect.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import cn.colgate.colgateconnect.auth.choose.ChooseAuthMethodFragment;
import cn.colgate.colgateconnect.auth.result.AuthenticationResultData;
import cn.colgate.colgateconnect.auth.sms.SmsAuthFragment;
import com.kolibree.android.app.ui.fragment.BaseDaggerFragment;
import com.kolibree.android.extensions.DisposableUtils;
import com.kolibree.android.translationssupport.Translations;
import com.kolibree.sdkws.sms.data.AccountData;
import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import java.util.Collections;
import javax.inject.Inject;
import timber.log.Timber;

@Keep
public class AuthenticationFlowActivity extends AppCompatActivity implements HasAndroidInjector {

  private static final String TAG = AuthenticationFlowActivity.class.getSimpleName();
  private static final String EXTRA_ACCOUNT_DATA = TAG + ".EXTRA_ACCOUNT_DATA";
  private static final String EXTRA_RESULT_DATA = TAG + ".EXTRA_RESULT_DATA";
  @Inject AuthenticationFlowNavigationController navigationController;
  @Inject DispatchingAndroidInjector<Object> fragmentInjector;
  private CompositeDisposable disposables = new CompositeDisposable();

  /**
   * Creates Intent object for account creation flow. Information about new account should be
   * provided by object AccountData.
   *
   * @param context
   * @param data contains all needed data for account creation process
   * @return Intent for account creation
   */
  public static Intent createAccountFlow(Context context, AccountData data) {
    Intent intent = new Intent(context, AuthenticationFlowActivity.class);
    intent.putExtra(EXTRA_ACCOUNT_DATA, data);
    return intent;
  }

  /**
   * Creates Intent object for login flow.
   *
   * @param context
   * @return Intent for login flow
   */
  public static Intent loginToAccountFlow(Context context) {
    return new Intent(context, AuthenticationFlowActivity.class);
  }

  /**
   * Checks if activity has been finished with success.
   *
   * @param resultCode value returned by Activity#onActivityResult
   * @return true if success, false otherwise
   */
  public static boolean wasSuccess(int resultCode) {
    return resultCode == Activity.RESULT_OK;
  }

  /**
   * Extracts AuthenticationResultData from data object, returned by Activity#onActivityResult
   *
   * @param data object returned by Activity#onActivityResult
   * @return AuthenticationResultData
   */
  @Nullable
  public static AuthenticationResultData extractResultData(Intent data) {
    if (data != null) {
      return data.getParcelableExtra(EXTRA_RESULT_DATA);
    }
    return new AuthenticationResultData(Collections.emptyList());
  }

  public AccountData extractAccountData() {
    Intent intent = getIntent();
    if (intent != null) {
      return intent.getParcelableExtra(EXTRA_ACCOUNT_DATA);
    }
    return null;
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    AndroidInjection.inject(this);

    super.onCreate(savedInstanceState);

    listenToNavigationController();

    setContentView(R.layout.activity_auth_flow);

    setResult(Activity.RESULT_CANCELED);

    showSmsFragment();
  }

  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(Translations.wrapContext(base));
  }

  private void listenToNavigationController() {
    DisposableUtils.addSafely(
        disposables,
        navigationController
            .navigateActionObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::changeNavigation, Timber::e));
  }

  private void changeNavigation(AuthenticationFlowNavigationController.NavigateAction action) {
    if (action.isFinishSuccess()) {
      finishWithResult(Activity.RESULT_OK, action.resultData());
    } else if (action.isSms()) {
      showSmsFragment();
    } else if (action.isWeChat()) {
      showWeChatFragment();
    } else if (action.isChooseAuthMethod()) {
      showChooseAuthMethodFragment();
    } else if (action.isFinishCanceled()) {
      finishWithResult(Activity.RESULT_CANCELED, action.resultData());
    }
  }

  private void showWeChatFragment() {
    Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show();
  }

  private void finishWithResult(int result, AuthenticationResultData data) {
    Intent intentData = new Intent();
    intentData.putExtra(EXTRA_RESULT_DATA, data);
    setResult(result, intentData);
    finish();
  }

  @Override
  protected void onDestroy() {
    disposables.clear();
    super.onDestroy();
  }

  private void showChooseAuthMethodFragment() {
    getSupportFragmentManager().popBackStackImmediate();
    showFragment(new ChooseAuthMethodFragment());
  }

  private void showSmsFragment() {
    showFragment(new SmsAuthFragment());
  }

  private void showFragment(BaseDaggerFragment fragment) {
    final String tag = fragment.getClass().getName();

    if (getSupportFragmentManager().getFragments().size() == 0) {
      getSupportFragmentManager()
          .beginTransaction()
          .add(R.id.auth_flow_container, fragment, tag)
          .commitNow();
    } else if (getSupportFragmentManager().findFragmentByTag(tag) == null) {
      getSupportFragmentManager()
          .beginTransaction()
          .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
          .replace(R.id.auth_flow_container, fragment, tag)
          .addToBackStack(null)
          .commit();
    }
  }

  @Override
  public void onBackPressed() {
    navigationController.onBackPressed();
  }

  @Override
  public AndroidInjector<Object> androidInjector() {
    return fragmentInjector;
  }
}

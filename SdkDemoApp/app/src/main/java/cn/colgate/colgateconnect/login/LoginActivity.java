package cn.colgate.colgateconnect.login;

import static android.widget.Toast.makeText;
import static com.kolibree.account.logout.LogoutEnforcerKt.EXTRA_FORCED_LOGOUT;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import butterknife.OnClick;
import cn.colgate.colgateconnect.R;
import cn.colgate.colgateconnect.auth.AuthenticationFlowActivity;
import cn.colgate.colgateconnect.auth.result.AuthenticationResultData;
import cn.colgate.colgateconnect.base.SdkDemoBaseActivity;
import cn.colgate.colgateconnect.demo.AccountInfo;
import cn.colgate.colgateconnect.demo.DataStore;
import cn.colgate.colgateconnect.home.MainActivity;
import cn.colgate.colgateconnect.register.RegisterActivity;
import cn.colgate.colgateconnect.wxapi.WXApiManager;
import com.kolibree.account.logout.ForceLogoutReason;
import com.kolibree.android.accountinternal.profile.models.IProfile;
import com.kolibree.sdkws.sms.SmsAccountManager;
import dagger.android.AndroidInjection;
import java.util.List;
import javax.inject.Inject;

/**
 * Demo app to show the usage of the SDK only. In your app , please pay attention to the error.
 * Usage of RxAndroid and dagger are required to use the SDK.
 */
public class LoginActivity extends SdkDemoBaseActivity {

  private static final int LOGIN_FLOW = 102;

  @Inject AccountInfo accountInfo;

  @Inject DataStore dataStore;

  @Inject SmsAccountManager smsAccountManager;

  public static void start(Context context) {
    Intent starter = new Intent(context, LoginActivity.class);
    context.startActivity(starter);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    checkProfileStored();
  }

  /** Check if there is a stored profile locally and connect him automatically if any */
  private void checkProfileStored() {
    IProfile profileStored = dataStore.getStoredProfile();
    if (profileStored != null) {
      goToHomePage(profileStored);
    } else {
      checkForcedLogout();
    }
  }

  private void checkForcedLogout() {
    ForceLogoutReason reason =
        (ForceLogoutReason) getIntent().getSerializableExtra(EXTRA_FORCED_LOGOUT);

    if (reason != null) {
      makeText(this, "Forced logout because " + reason, Toast.LENGTH_LONG).show();
    }
  }

  @OnClick(R.id.tvLogin)
  void login() {
    // display the sms auth login flow using the authentication module
    Intent intent = AuthenticationFlowActivity.loginToAccountFlow(this);
    startActivityForResult(intent, LOGIN_FLOW);
  }

  @OnClick(R.id.tvLoginWeChat)
  void WeChatLogin() {
    WXApiManager.getInstance().setCurrentRequestCode(WXApiManager.REQUEST_CODE_LOGIN_WECHAT);
    WXApiManager.getInstance().requestWeChatCode(this);
    finish();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    if (requestCode == LOGIN_FLOW) {
      if (resultCode == Activity.RESULT_OK) {
        if (AuthenticationFlowActivity.wasSuccess(resultCode)) {
          AuthenticationResultData result = AuthenticationFlowActivity.extractResultData(data);
          if (result != null) {
            List<IProfile> profiles = result.getProfiles();
            if (!profiles.isEmpty()) {
              goToHomePage(profiles.get(0));
            } else {
              makeText(this, "No profile associated to this account", Toast.LENGTH_LONG).show();
            }
          } else {
            makeText(this, "No data received !", Toast.LENGTH_LONG).show();
          }

        } else {
          makeText(this, "Login Not possible", Toast.LENGTH_LONG).show();
        }
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  @OnClick(R.id.tvRegister)
  void register() {
    RegisterActivity.start(this);
    finish();
  }

  private void goToHomePage(IProfile profile) {
    hideProgress();
    accountInfo.setCurrentProfile(profile); // store the profile in the singleton
    dataStore.storeProfile(profile); // store the profile locally
    MainActivity.start(this);
    finish();
  }
}

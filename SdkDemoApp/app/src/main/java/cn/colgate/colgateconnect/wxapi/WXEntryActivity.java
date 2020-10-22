package cn.colgate.colgateconnect.wxapi;

import static com.kolibree.android.commons.ApiConstants.DATETIME_FORMATTER;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import cn.colgate.colgateconnect.base.SdkDemoBaseActivity;
import cn.colgate.colgateconnect.demo.AccountInfo;
import cn.colgate.colgateconnect.demo.DataStore;
import cn.colgate.colgateconnect.home.MainActivity;
import cn.colgate.colgateconnect.login.LoginActivity;
import com.kolibree.account.AccountFacade;
import com.kolibree.android.accountinternal.profile.models.IProfile;
import com.kolibree.android.clock.TrustedClock;
import com.kolibree.android.commons.profile.Gender;
import com.kolibree.android.commons.profile.Handedness;
import com.kolibree.android.commons.profile.SourceApplication;
import com.kolibree.android.network.api.ApiError;
import com.kolibree.sdkws.brushing.wrapper.IBrushing;
import com.kolibree.sdkws.exception.WeChatAccountNotRecognizedException;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import dagger.android.AndroidInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import javax.inject.Inject;
import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;
import org.threeten.bp.LocalDate;
import timber.log.Timber;

/* Do not rename this class */
public class WXEntryActivity extends SdkDemoBaseActivity implements IWXAPIEventHandler {

  @Inject AccountInfo accountInfo;

  @Inject DataStore dataStore;

  @Inject AccountFacade accountFacade;

  private WXApiManager wxApiManager;

  public static void start(Context context) {
    Intent starter = new Intent(context, WXEntryActivity.class);
    context.startActivity(starter);
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
    wxApiManager = WXApiManager.getInstance();
    checkIntentValidity();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    wxApiManager.handleIntent(getIntent(), this);
  }

  private void checkIntentValidity() {
    // Check the value of handleIntent. If the value is false ，the illegal parameter will be ignored
    // by WeChat SDK but activity will stay as it.
    // So Activity should be finished to prevent user confused if the intent is invalid.
    try {
      boolean result = wxApiManager.handleIntent(getIntent(), this);
      if (!result) {
        Timber.d("Illegal parameters，finish");
        fallBackToLoginActivity();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void linkWeChat(String code) {
    disposables.add(
        accountFacade
            .linkWeChat(code)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                () ->
                    Toast.makeText(
                            this,
                            "Success! Code:"
                                + accountFacade.getAccount().getWeChatData().getOpenId(),
                            Toast.LENGTH_LONG)
                        .show(),
                this::onBackendError));
  }

  private void attemptLoginWeChat(@NonNull String code) {
    disposables.add(
        accountFacade
            .attemptLoginWithWechat(code)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                accountInUse -> {
                  if (wxApiManager.getCurrentRequestCode()
                      == WXApiManager.REQUEST_CODE_CHECK_ACCOUNT_EXIST) {
                    postWeChatId(accountInUse.getWeChatData().getOpenId());
                  } else {
                    Toast.makeText(
                            this, "Successfully logged with WeChat account ", Toast.LENGTH_LONG)
                        .show();
                    goToHomePage(accountInUse.getProfiles().get(0));
                  }
                },
                error -> onLoginError(code, error)));
  }

  private void postWeChatId(String id) {
    Toast.makeText(this, "WeChat account exists", Toast.LENGTH_LONG).show();
    EventBus.getDefault().post(new WeChatEvent(id));
    finish();
  }

  private void onLoginError(String code, Throwable error) {
    if (error instanceof WeChatAccountNotRecognizedException) {
      onWeChatError(code, error);
    } else if (error instanceof ApiError) {
      onBackendError(error);
    } else {
      error.printStackTrace();
      fallBackToLoginActivity();
    }
  }

  private void onWeChatError(String code, Throwable error) {
    String token = ((WeChatAccountNotRecognizedException) error).getLoginAttemptToken();
    if (wxApiManager.getCurrentRequestCode() == WXApiManager.REQUEST_CODE_CHECK_ACCOUNT_EXIST) {
      Toast.makeText(this, "Not in use.", Toast.LENGTH_LONG).show();
      return;
    }
    registerWithWeChat(code, token);
    Toast.makeText(this, "Register now!", Toast.LENGTH_LONG).show();
  }

  private void onBackendError(Throwable error) {
    Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
    fallBackToLoginActivity();
  }

  private void fallBackToLoginActivity() {
    LoginActivity.start(this);
    finish();
  }

  /** register Wechat with demo profile */
  private void registerWithWeChat(String code, String token) {
    IProfile demo = getDemoProfile();
    disposables.add(
        accountFacade
            .registerWithWechatWithToken(code, token, demo)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                newProfile -> {
                  Timber.i("here is my profile after register  : %s", newProfile.toString());
                  goToHomePage(newProfile.getProfiles().get(0));
                },
                cn.colgate.colgateconnect.utils.ApiError::displayErrorMessage));
  }

  private IProfile getDemoProfile() {
    return new IProfile() {
      @Override
      public long getId() {
        return 0;
      }

      @Override
      public @NotNull String getFirstName() {
        return "Demo";
      }

      @Override
      public @NotNull Gender getGender() {
        return Gender.MALE;
      }

      @Override
      public @NotNull Handedness getHandedness() {
        return Handedness.RIGHT_HANDED;
      }

      @Override
      public int getBrushingGoalTime() {
        return IBrushing.MINIMUM_BRUSHING_GOAL_TIME_SECONDS;
      }

      @Override
      public @NotNull String getCreatedDate() {
        return TrustedClock.getNowZonedDateTime().format(DATETIME_FORMATTER);
      }

      @Override
      public LocalDate getBirthday() {
        return LocalDate.of(2000, 1, 1);
      }

      @Nullable
      @Override
      public String getPictureUrl() {
        return null;
      }

      @Nullable
      @Override
      public String getPictureLastModifier() {
        return null;
      }

      @Override
      public String getCountry() {
        return "cn";
      }

      @Override
      public boolean isMale() {
        return true;
      }

      @Override
      public boolean isRightHanded() {
        return true;
      }

      @Nullable
      @Override
      public SourceApplication getSourceApplication() {
        return null;
      }
    };
  }

  private void goToHomePage(IProfile profile) {
    hideProgress();
    accountInfo.setCurrentProfile(profile); // store the profile in the singleton
    dataStore.storeProfile(profile); // store the profile locally
    MainActivity.start(this);
    finish();
  }

  @Override
  public void onReq(BaseReq baseReq) {}

  @Override
  public void onResp(BaseResp baseResp) {
    SendAuth.Resp response = (SendAuth.Resp) baseResp;
    if (response.errCode != 0) {
      Toast.makeText(this, "WeChat error: " + response.errCode, Toast.LENGTH_LONG).show();
      fallBackToLoginActivity();
    } else {
      if (WXApiManager.getInstance().getCurrentRequestCode()
          == WXApiManager.REQUEST_CODE_LINK_WECHAT) {
        linkWeChat(response.code);
      } else {
        attemptLoginWeChat(response.code);
      }
    }
  }
}

package cn.colgate.colgateconnect.auth;

import android.annotation.SuppressLint;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import cn.colgate.colgateconnect.auth.result.AuthenticationResultData;
import com.jakewharton.rxrelay2.PublishRelay;
import com.kolibree.android.app.dagger.scopes.ActivityScope;
import io.reactivex.Observable;
import javax.inject.Inject;

@SuppressLint({"SdkPublicClassInNonKolibreePackage", "DeobfuscatedPublicSdkClass"})
@ActivityScope
public class AuthenticationFlowNavigationController {

  private final PublishRelay<NavigateAction> navigateActionRelay = PublishRelay.create();

  private final Observable<NavigateAction> navigateActionObservable =
      navigateActionRelay.publish().autoConnect();

  Observable<NavigateAction> navigateActionObservable() {
    return navigateActionObservable;
  }

  @Inject
  public AuthenticationFlowNavigationController() {}

  public void onBackPressed() {
    finishCanceled();
  }

  public void navigateToSmsScreen() {
    navigate(NavigateAction.SMS);
  }

  public void navigateToWeChatScreen() {
    navigate(NavigateAction.WE_CHAT);
  }

  public void finishSuccess(AuthenticationResultData data) {
    navigate(NavigateAction.FINISH_SUCCESS, data);
  }

  @VisibleForTesting
  void finishCanceled() {
    navigate(NavigateAction.FINISH_CANCELED);
  }

  @VisibleForTesting
  void navigateToChooseAuthMethod() {
    navigate(NavigateAction.CHOOSE_AUTH_METHOD);
  }

  private void navigate(int action, AuthenticationResultData data) {
    NavigateAction navigateAction = new NavigateAction(action, data);
    navigateActionRelay.accept(navigateAction);
  }

  private void navigate(int action) {
    NavigateAction navigateAction = new NavigateAction(action, null);
    navigateActionRelay.accept(navigateAction);
  }

  static class NavigateAction {

    private static int SMS = 0;
    private static int WE_CHAT = 1;
    private static int CHOOSE_AUTH_METHOD = 2;
    private static int FINISH_SUCCESS = 3;
    private static int FINISH_CANCELED = 4;

    private final int actionId;
    private final AuthenticationResultData data;

    public NavigateAction(int actionId, AuthenticationResultData data) {
      this.actionId = actionId;
      this.data = data;
    }

    public boolean isSms() {
      return actionId == SMS;
    }

    public boolean isWeChat() {
      return actionId == WE_CHAT;
    }

    public boolean isFinishSuccess() {
      return actionId == FINISH_SUCCESS;
    }

    public boolean isFinishCanceled() {
      return actionId == FINISH_CANCELED;
    }

    public boolean isChooseAuthMethod() {
      return actionId == CHOOSE_AUTH_METHOD;
    }

    @Nullable
    public AuthenticationResultData resultData() {
      return data;
    }
  }
}

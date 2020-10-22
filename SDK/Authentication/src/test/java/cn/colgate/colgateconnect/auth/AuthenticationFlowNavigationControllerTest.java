package cn.colgate.colgateconnect.auth;

import cn.colgate.colgateconnect.auth.AuthenticationFlowNavigationController.NavigateAction;
import com.kolibree.android.app.test.BaseUnitTest;
import io.reactivex.observers.TestObserver;
import org.junit.Test;

public class AuthenticationFlowNavigationControllerTest extends BaseUnitTest {

  AuthenticationFlowNavigationController navigationController;

  @Override
  public void setup() throws Exception {
    super.setup();
    navigationController = new AuthenticationFlowNavigationController();
  }

  @Test
  public void navigateToSmsAccountScreen_emits() throws Exception {
    TestObserver<NavigateAction> test = navigationController.navigateActionObservable().test();
    navigationController.navigateToSmsScreen();
    test.assertValue(NavigateAction::isSms);
  }

  @Test
  public void navigateToWeChatAccountScreen_emits() throws Exception {
    TestObserver<NavigateAction> test = navigationController.navigateActionObservable().test();
    navigationController.navigateToWeChatScreen();
    test.assertValue(NavigateAction::isWeChat);
  }

  @Test
  public void finishSuccess_emits() throws Exception {
    TestObserver<NavigateAction> test = navigationController.navigateActionObservable().test();
    navigationController.finishSuccess(null);
    test.assertValue(NavigateAction::isFinishSuccess);
  }

  @Test
  public void finishCanceled_emits() throws Exception {
    TestObserver<NavigateAction> test = navigationController.navigateActionObservable().test();
    navigationController.finishCanceled();
    test.assertValue(NavigateAction::isFinishCanceled);
  }

  @Test
  public void navigateToChooseAuthScreen_emits() throws Exception {
    TestObserver<NavigateAction> test = navigationController.navigateActionObservable().test();
    navigationController.navigateToChooseAuthMethod();
    test.assertValue(NavigateAction::isChooseAuthMethod);
  }
}

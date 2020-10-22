package cn.colgate.colgateconnect.auth.choose;

import cn.colgate.colgateconnect.auth.AuthenticationFlowNavigationController;
import com.kolibree.android.app.test.BaseUnitTest;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class ChooseAuthMethodViewModelTest extends BaseUnitTest {

  ChooseAuthMethodViewModel viewModel;

  @Mock AuthenticationFlowNavigationController navigationController;

  @Override
  public void setup() throws Exception {
    super.setup();

    viewModel = Mockito.spy(new ChooseAuthMethodViewModel(navigationController));
  }

  @Test
  public void userClickedSmsMethod_invokes_navigateToSmsAccountScreen() {
    viewModel.userClickedSmsMethod();
    Mockito.verify(navigationController).navigateToSmsScreen();
  }

  @Test
  public void userClickedWeChatMethod_invokes_navigateToWeChatAccountScreen() {
    viewModel.userClickedWeChatMethod();
    Mockito.verify(navigationController).navigateToWeChatScreen();
  }
}

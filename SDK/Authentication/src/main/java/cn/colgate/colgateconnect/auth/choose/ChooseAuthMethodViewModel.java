package cn.colgate.colgateconnect.auth.choose;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import cn.colgate.colgateconnect.auth.AuthenticationFlowNavigationController;
import javax.inject.Inject;

@SuppressLint({"SdkPublicClassInNonKolibreePackage", "DeobfuscatedPublicSdkClass"})
public class ChooseAuthMethodViewModel extends ViewModel {

  private final AuthenticationFlowNavigationController navigationController;

  ChooseAuthMethodViewModel(AuthenticationFlowNavigationController navigationController) {
    this.navigationController = navigationController;
  }

  public void userClickedSmsMethod() {
    navigationController.navigateToSmsScreen();
  }

  public void userClickedWeChatMethod() {
    navigationController.navigateToWeChatScreen();
  }

  static class Factory implements ViewModelProvider.Factory {

    private final AuthenticationFlowNavigationController navigationController;

    @Inject
    Factory(@NonNull AuthenticationFlowNavigationController navigationController) {
      this.navigationController = navigationController;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    @Override
    public ChooseAuthMethodViewModel create(@NonNull Class modelClass) {
      return new ChooseAuthMethodViewModel(navigationController);
    }
  }
}

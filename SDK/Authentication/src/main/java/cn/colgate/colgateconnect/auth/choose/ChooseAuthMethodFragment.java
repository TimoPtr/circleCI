package cn.colgate.colgateconnect.auth.choose;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import cn.colgate.colgateconnect.auth.AuthenticationFlowNavigationController;
import cn.colgate.colgateconnect.auth.R;
import cn.colgate.colgateconnect.auth.choose.ChooseAuthMethodViewModel.Factory;
import com.kolibree.android.app.ui.fragment.BaseDaggerFragment;
import javax.inject.Inject;

@SuppressLint({"SdkPublicClassInNonKolibreePackage", "DeobfuscatedPublicSdkClass"})
public final class ChooseAuthMethodFragment extends BaseDaggerFragment {

  @Inject Factory viewModelFactory;

  @Inject AuthenticationFlowNavigationController navigationController;

  private ChooseAuthMethodViewModel viewModel;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflateView(inflater, container, R.layout.fragment_choose_login_method);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initViews(view);
    initViewModel();
    initToolbar(view.findViewById(R.id.toolbar));
  }

  private void initViews(View root) {
    root.findViewById(R.id.choose_login_sms).setOnClickListener(v -> onSmsMethodClick());
    root.findViewById(R.id.choose_login_we_chat).setOnClickListener(v -> onWeChatMethodClick());
  }

  private void initViewModel() {
    viewModel = ViewModelProviders.of(this, viewModelFactory).get(ChooseAuthMethodViewModel.class);
  }

  private void initToolbar(Toolbar toolbar) {
    toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
    toolbar.setNavigationOnClickListener(v -> onBackPressed());
  }

  private void onBackPressed() {
    navigationController.onBackPressed();
  }

  void onSmsMethodClick() {
    viewModel.userClickedSmsMethod();
  }

  void onWeChatMethodClick() {
    viewModel.userClickedWeChatMethod();
  }
}

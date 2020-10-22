package cn.colgate.colgateconnect.auth.sms;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import cn.colgate.colgateconnect.auth.AuthenticationFlowNavigationController;
import cn.colgate.colgateconnect.auth.R;
import com.google.android.material.textfield.TextInputLayout;
import com.kolibree.android.app.ui.fragment.BaseDaggerFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import javax.inject.Inject;

@SuppressLint({"SdkPublicClassInNonKolibreePackage", "DeobfuscatedPublicSdkClass"})
public final class SmsAuthFragment extends BaseDaggerFragment {

  @Inject AuthenticationFlowNavigationController navigationController;

  @Inject SmsAuthViewModel.Factory viewModelFactory;

  private TextInputLayout phoneNumberInputLayout;
  private EditText phoneNumber;
  private Button sendPhoneNumber;
  private TextView toolbarOk;
  private EditText confirmationCode;
  private TextView confirmationCodeError;
  private View confirmationCodeContainer;
  private View progress;
  private SmsAuthViewModel viewModel;
  private Disposable viewModelObservableDisposable;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflateView(inflater, container, R.layout.fragment_sms_auth);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initViews(view);
    initListeners();
    initViewModel();
    initToolbar(view.findViewById(R.id.sms_login_toolbar));
  }

  private void initViews(View view) {
    phoneNumberInputLayout = view.findViewById(R.id.sms_login_phone_number_layout);
    phoneNumber = view.findViewById(R.id.sms_login_phone_number);
    sendPhoneNumber = view.findViewById(R.id.sms_login_get_code);
    toolbarOk = view.findViewById(R.id.sms_login_toolbar_ok);
    confirmationCode = view.findViewById(R.id.sms_login_confirmation_code);
    confirmationCodeError = view.findViewById(R.id.sms_login_confirmation_code_error);
    confirmationCodeContainer = view.findViewById(R.id.sms_login_confirmation_code_container);
    progress = view.findViewById(R.id.sms_login_progress);
  }

  private void initListeners() {
    phoneNumber.setOnEditorActionListener((v, actionId, event) -> onProvidedPhoneNumberAction());
    sendPhoneNumber.setOnClickListener(v -> onSendPhoneNumberClicked());
    toolbarOk.setOnClickListener(v -> onOkClicked());
    confirmationCode.setOnEditorActionListener(
        (v, actionId, event) -> onProvidedConfirmationCodeAction());
  }

  private void initViewModel() {
    viewModel = ViewModelProviders.of(this, viewModelFactory).get(SmsAuthViewModel.class);
  }

  private void initToolbar(Toolbar toolbar) {
    toolbar.setTitle(R.string.sms_login_title);
    toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
    toolbar.setNavigationOnClickListener(v -> onBackPressed());
  }

  private void onBackPressed() {
    navigationController.onBackPressed();
  }

  @Override
  public void onResume() {
    super.onResume();

    viewModelObservableDisposable =
        viewModel
            .getViewStateObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::render, Throwable::printStackTrace);
  }

  @Override
  public void onPause() {
    viewModelObservableDisposable.dispose();
    super.onPause();
  }

  private void render(SmsAuthViewState viewState) {
    renderPhoneNumber(viewState);
    renderConfirmationCode(viewState);
    renderToolbarOk(viewState);
    renderPhoneNumberError(viewState);
    renderConfirmationCodeError(viewState);
    renderLoading(viewState);
  }

  private void renderToolbarOk(SmsAuthViewState viewState) {
    boolean isEnabled = viewState.isConfirmationCodeVisible() && !viewState.isLoading();
    toolbarOk.setEnabled(isEnabled);
    int color = isEnabled ? R.color.white : R.color.kolibree_disabled;
    toolbarOk.setTextColor(ContextCompat.getColor(getContext(), color));
  }

  private void renderPhoneNumber(SmsAuthViewState viewState) {
    phoneNumber.setEnabled(!viewState.isConfirmationCodeVisible());
    sendPhoneNumber.setEnabled(!viewState.isConfirmationCodeVisible());
  }

  private void showConfirmationCodeIfNotVisible() {
    if (confirmationCodeContainer.getVisibility() != View.VISIBLE) {
      confirmationCodeContainer.setAlpha(0f);
      confirmationCodeContainer.setVisibility(View.VISIBLE);
      confirmationCodeContainer.animate().alpha(1f);
    }
  }

  private void renderConfirmationCode(SmsAuthViewState viewState) {
    if (viewState.isConfirmationCodeVisible()) {
      showConfirmationCodeIfNotVisible();
      confirmationCode.setEnabled(true);
    } else {
      confirmationCodeContainer.setVisibility(View.INVISIBLE);
      confirmationCode.setEnabled(false);
    }
  }

  private void renderLoading(SmsAuthViewState viewState) {
    progress.setVisibility(viewState.isLoading() ? View.VISIBLE : View.INVISIBLE);
    if (viewState.isLoading()) {
      hideKeyboard(confirmationCode);
      hideKeyboard(phoneNumber);
      phoneNumber.setEnabled(false);
      sendPhoneNumber.setEnabled(false);
      confirmationCode.setEnabled(false);
      toolbarOk.setEnabled(false);
    }
  }

  private void renderPhoneNumberError(SmsAuthViewState viewState) {
    if (hasError(viewState.getPhoneNumberError())) {
      phoneNumberInputLayout.setError(viewState.getPhoneNumberError());
    } else {
      phoneNumberInputLayout.setError(null);
    }
  }

  private void renderConfirmationCodeError(SmsAuthViewState viewState) {
    if (hasError(viewState.getConfirmationCodeError())) {
      confirmationCodeError.setText(viewState.getConfirmationCodeError());
      confirmationCodeError.setVisibility(View.VISIBLE);
    } else {
      confirmationCodeError.setVisibility(View.INVISIBLE);
      confirmationCodeError.setText("");
    }
  }

  private boolean hasError(@Nullable String error) {
    return error != null && !error.isEmpty();
  }

  private void hideKeyboard(View view) {
    if (view.getWindowToken() != null && getActivity() != null) {
      InputMethodManager inputMethodManager =
          (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
      inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
      view.clearFocus();
    }
  }

  boolean onProvidedConfirmationCodeAction() {
    onOkClicked();
    return false;
  }

  boolean onProvidedPhoneNumberAction() {
    String textualPhoneNumber = phoneNumber.getText().toString();
    viewModel.userProvidedPhoneNumber(textualPhoneNumber);
    return false;
  }

  void onSendPhoneNumberClicked() {
    String textualPhoneNumber = phoneNumber.getText().toString();
    viewModel.userProvidedPhoneNumber(textualPhoneNumber);
  }

  void onOkClicked() {
    String textualCode = confirmationCode.getText().toString();
    viewModel.userProvidedConfirmationCode(textualCode);
  }
}

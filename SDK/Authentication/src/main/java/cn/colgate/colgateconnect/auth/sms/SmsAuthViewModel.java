package cn.colgate.colgateconnect.auth.sms;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import cn.colgate.colgateconnect.auth.AuthenticationFlowNavigationController;
import cn.colgate.colgateconnect.auth.R;
import cn.colgate.colgateconnect.auth.result.AuthenticationResultData;
import com.jakewharton.rxrelay2.BehaviorRelay;
import com.kolibree.android.accountinternal.profile.models.IProfile;
import com.kolibree.android.extensions.DisposableUtils;
import com.kolibree.android.network.api.ApiError;
import com.kolibree.android.utils.PhoneNumberChecker;
import com.kolibree.sdkws.core.IKolibreeConnector;
import com.kolibree.sdkws.sms.SmsAccountManager;
import com.kolibree.sdkws.sms.SmsToken;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import javax.inject.Inject;
import timber.log.Timber;

@SuppressLint("SdkPublicClassInNonKolibreePackage")
class SmsAuthViewModel extends ViewModel {

  private final BehaviorRelay<SmsAuthViewState> viewStateBehaviorRelay = BehaviorRelay.create();
  private final CompositeDisposable disposables = new CompositeDisposable();
  private final SmsAccountManager smsAccountManager;
  private final SmsAuthFlow accountFlow;
  private final AuthenticationFlowNavigationController navigationController;
  private final PhoneNumberChecker phoneNumberChecker;
  private final Resources resources;
  private volatile Observable<SmsAuthViewState> viewStateObservable;
  private SmsAuthViewState viewState = new SmsAuthViewState();
  private SmsToken smsToken;

  SmsAuthViewModel(
      @NonNull SmsAccountManager smsAccountManager,
      @NonNull SmsAuthFlow accountFlow,
      @NonNull AuthenticationFlowNavigationController navigationController,
      @NonNull PhoneNumberChecker phoneNumberChecker,
      @NonNull Resources resources) {
    this.smsAccountManager = smsAccountManager;
    this.accountFlow = accountFlow;
    this.navigationController = navigationController;
    this.phoneNumberChecker = phoneNumberChecker;
    this.resources = resources;
    this.smsToken = new SmsToken();
  }

  @NonNull
  final Observable<SmsAuthViewState> getViewStateObservable() {
    if (viewStateObservable == null) {
      synchronized (this) {
        if (viewStateObservable == null) {
          viewStateObservable =
              viewStateBehaviorRelay.startWith(viewState).doAfterNext(this::storeViewState).share();
        }
      }
    }

    return viewStateObservable;
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    disposables.dispose();
  }

  private void storeViewState(SmsAuthViewState newViewState) {
    viewState = newViewState;
  }

  public void userProvidedPhoneNumber(String phoneNumber) {
    if (!phoneNumberChecker.isValid(phoneNumber)) {
      emitInvalidPhoneNumberError();
      return;
    }

    DisposableUtils.addSafely(
        disposables,
        smsAccountManager
            .sendSmsCodeTo(phoneNumber)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe(ignore -> emitLoadingViewState())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::smsCodeSuccess, this::smsCodeError));
  }

  private void emitInvalidPhoneNumberError() {
    emitViewState(
        viewState.withPhoneNumberError(resources.getString(R.string.sms_login_invalid_number)));
  }

  @VisibleForTesting
  void smsCodeSuccess(SmsToken token) {
    this.smsToken = token;

    emitViewState(viewState.withIsLoading(false).withIsConfirmationCodeVisible(true).clearErrors());
  }

  @VisibleForTesting
  void smsCodeError(Throwable throwable) {
    emitViewState(
        viewState
            .clearErrors()
            .withIsLoading(false)
            .withPhoneNumberError(getBackendError(throwable)));
  }

  public void userProvidedConfirmationCode(String verificationCode) {
    DisposableUtils.addSafely(
        disposables,
        accountFlow
            .execute(smsToken, verificationCode)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe(
                ignore ->
                    emitViewState(
                        viewState
                            .withIsLoading(true)
                            .withIsConfirmationCodeVisible(true)
                            .clearErrors()))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::accountBySmsSuccess, this::accountBySmsError));
  }

  @VisibleForTesting
  void accountBySmsSuccess(List<IProfile> listOfProfiles) {
    navigationController.finishSuccess(new AuthenticationResultData(listOfProfiles));
  }

  @VisibleForTesting
  void accountBySmsError(Throwable throwable) {
    Timber.e(throwable);

    emitViewState(
        viewState
            .clearErrors()
            .withIsLoading(false)
            .withConfirmationCodeError(getBackendError(throwable)));
  }

  @VisibleForTesting
  String getBackendError(Throwable throwable) {
    if (throwable instanceof CompositeException) {
      CompositeException composite = (CompositeException) throwable;
      for (Throwable t : composite.getExceptions()) {
        if (t instanceof ApiError) {
          ApiError apiError = (ApiError) t;
          String errorMessage = apiError.getDisplayableMessage();
          if (errorMessage != null && !errorMessage.isEmpty()) {
            return errorMessage;
          }
        }
      }
    }
    return ApiError.generateUnknownError().getMessage();
  }

  @VisibleForTesting
  void emitLoadingViewState() {
    emitViewState(viewState.withIsLoading(true).clearErrors());
  }

  private void emitViewState(SmsAuthViewState newViewState) {
    viewStateBehaviorRelay.accept(newViewState);
  }

  static class Factory implements ViewModelProvider.Factory {

    private final IKolibreeConnector connector;
    private final SmsAuthFlow accountFlow;
    private final AuthenticationFlowNavigationController navigationController;
    private final PhoneNumberChecker phoneNumberChecker;
    private final Resources resources;

    @Inject
    Factory(
        @NonNull IKolibreeConnector connector,
        @Nullable SmsAuthFlow accountFlow,
        @NonNull AuthenticationFlowNavigationController navigationController,
        @NonNull PhoneNumberChecker phoneNumberChecker,
        @NonNull Context context) {
      this.connector = connector;
      this.accountFlow = accountFlow;
      this.navigationController = navigationController;
      this.phoneNumberChecker = phoneNumberChecker;
      this.resources = context.getResources();
    }

    @NonNull
    @SuppressWarnings("unchecked")
    @Override
    public SmsAuthViewModel create(@NonNull Class modelClass) {
      return new SmsAuthViewModel(
          connector, accountFlow, navigationController, phoneNumberChecker, resources);
    }
  }
}

package cn.colgate.colgateconnect.auth.sms;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

import android.content.res.Resources;
import cn.colgate.colgateconnect.auth.AuthenticationFlowNavigationController;
import com.kolibree.android.accountinternal.profile.models.IProfile;
import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.android.failearly.FailEarly;
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate;
import com.kolibree.android.utils.PhoneNumberChecker;
import com.kolibree.sdkws.sms.SmsAccountManager;
import com.kolibree.sdkws.sms.SmsToken;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class SmsAuthViewModelTest extends BaseUnitTest {

  @Mock SmsAccountManager manager;

  @Mock SmsAuthFlow flow;

  @Mock AuthenticationFlowNavigationController navigationController;

  @Mock PhoneNumberChecker phoneNumberChecker;

  @Mock Resources resources;

  private SmsAuthViewModel viewModel;

  @Override
  public void setup() throws Exception {
    super.setup();

    FailEarly.overrideDelegateWith(NoopTestDelegate.INSTANCE);

    viewModel =
        Mockito.spy(
            new SmsAuthViewModel(
                manager, flow, navigationController, phoneNumberChecker, resources));
  }

  @Test
  public void getViewStateObservable_startsWithInitialViewState() {
    viewModel.getViewStateObservable().test().assertValue(new SmsAuthViewState());
  }

  @Test
  public void userProvidedPhoneNumber_success_SmsLoginViewState() {
    final String phoneNumber = "100-999-100";
    final String token = "--T0ken++";
    Mockito.when(manager.sendSmsCodeTo(phoneNumber))
        .thenReturn(Single.just(new SmsToken(phoneNumber, token)));
    Mockito.when(phoneNumberChecker.isValid(phoneNumber)).thenReturn(true);

    TestObserver<SmsAuthViewState> test = viewModel.getViewStateObservable().test();
    viewModel.userProvidedPhoneNumber(phoneNumber);
    test.assertValueAt(1, new SmsAuthViewState().withIsLoading(true));
    test.assertValueAt(
        2, new SmsAuthViewState().withIsLoading(false).withIsConfirmationCodeVisible(true));
  }

  @Test
  public void userProvidedPhoneNumber_success_invokesSmsCodeSuccess() {
    final String phoneNumber = "100-999-100";
    final String token = "--T0ken++";
    final SmsToken smsToken = new SmsToken(phoneNumber, token);
    Mockito.when(manager.sendSmsCodeTo(phoneNumber)).thenReturn(Single.just(smsToken));
    Mockito.when(phoneNumberChecker.isValid(phoneNumber)).thenReturn(true);

    viewModel.userProvidedPhoneNumber(phoneNumber);
    Mockito.verify(viewModel).smsCodeSuccess(smsToken);
  }

  @Test
  public void userProvidedPhoneNumber_success_invokesConnectorSendSmsCode() {
    final String phoneNumber = "100-999-100";
    final String token = "--T0ken++";
    final SmsToken smsToken = new SmsToken(phoneNumber, token);
    Mockito.when(manager.sendSmsCodeTo(phoneNumber)).thenReturn(Single.just(smsToken));
    Mockito.when(phoneNumberChecker.isValid(phoneNumber)).thenReturn(true);

    viewModel.userProvidedPhoneNumber(phoneNumber);
    Mockito.verify(manager).sendSmsCodeTo(phoneNumber);
  }

  @Test
  public void userProvidedPhoneNumber_error() {
    final String phoneNumber = "100-999-100";
    final String error = "Whoops..error";
    Throwable throwable = new Exception();
    Mockito.when(manager.sendSmsCodeTo(phoneNumber)).thenReturn(Single.error(throwable));
    Mockito.when(viewModel.getBackendError(any())).thenReturn(error);
    Mockito.when(phoneNumberChecker.isValid(phoneNumber)).thenReturn(true);

    TestObserver<SmsAuthViewState> test = viewModel.getViewStateObservable().test();
    viewModel.userProvidedPhoneNumber(phoneNumber);
    test.assertValueAt(1, new SmsAuthViewState().withIsLoading(true));
    test.assertValueAt(
        2,
        new SmsAuthViewState()
            .withIsLoading(false)
            .withIsConfirmationCodeVisible(false)
            .withPhoneNumberError(error));

    Mockito.verify(viewModel).smsCodeError(throwable);
  }

  @Test
  public void userProvidedPhoneNumber_error_invalidPhoneNumber() {
    final String phoneNumber = "100-999-100";
    final String error = "Invalid phone number";
    Mockito.when(phoneNumberChecker.isValid(phoneNumber)).thenReturn(false);
    Mockito.when(resources.getString(anyInt())).thenReturn(error);

    TestObserver<SmsAuthViewState> test = viewModel.getViewStateObservable().test();
    viewModel.userProvidedPhoneNumber(phoneNumber);
    test.assertValueAt(1, new SmsAuthViewState().withIsLoading(false).withPhoneNumberError(error));
  }

  @Test
  public void userProvidedConfirmationCode_login_success_SmsLoginViewState() {
    Mockito.when(manager.loginToAccount(any(), anyString()))
        .thenReturn(Single.just(Collections.emptyList()));
    Mockito.when(flow.execute(any(), any())).thenReturn(Single.just(Collections.emptyList()));

    TestObserver<SmsAuthViewState> test = viewModel.getViewStateObservable().test();
    viewModel.userProvidedConfirmationCode("223344");
    test.assertValueAt(
        1, new SmsAuthViewState().withIsLoading(true).withIsConfirmationCodeVisible(true));
  }

  @Test
  public void userProvidedConfirmationCode_login_success_invokesAccountBySmsSuccess() {
    IProfile profile = Mockito.mock(IProfile.class);
    List<IProfile> profiles = Collections.singletonList(profile);
    Mockito.when(flow.execute(any(), any())).thenReturn(Single.just(profiles));
    viewModel.userProvidedConfirmationCode("223344");
    Mockito.verify(viewModel).accountBySmsSuccess(profiles);
  }

  @Test
  public void userProvidedConfirmationCode_login_error() {
    final String error = "Whoops..error";
    Throwable throwable = new Exception();
    Mockito.when(flow.execute(any(), any())).thenReturn(Single.error(throwable));
    Mockito.when(viewModel.getBackendError(any())).thenReturn(error);

    TestObserver<SmsAuthViewState> test = viewModel.getViewStateObservable().test();
    viewModel.userProvidedConfirmationCode("1122");
    test.assertValueAt(
        1, new SmsAuthViewState().withIsLoading(true).withIsConfirmationCodeVisible(true));

    Mockito.verify(viewModel).accountBySmsError(throwable);
  }
}

/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.sdkws.core;

import static com.kolibree.android.network.api.ApiErrorCode.ACCOUNT_ADDITIONAL_INFO_NEEDED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.kolibree.android.accountinternal.CurrentProfileProvider;
import com.kolibree.android.accountinternal.account.ParentalConsent;
import com.kolibree.android.accountinternal.internal.AccountInternal;
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore;
import com.kolibree.android.accountinternal.profile.models.IProfile;
import com.kolibree.android.accountinternal.profile.models.Profile;
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal;
import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.android.clock.TrustedClock;
import com.kolibree.android.commons.profile.Gender;
import com.kolibree.android.commons.profile.Handedness;
import com.kolibree.android.network.api.ApiError;
import com.kolibree.android.network.api.ApiErrorCode;
import com.kolibree.sdkws.account.InternalAccountManager;
import com.kolibree.sdkws.api.request.Request;
import com.kolibree.sdkws.api.response.VerificationTokenResponse;
import com.kolibree.sdkws.brushing.BrushingSharedHelperKt;
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository;
import com.kolibree.sdkws.data.model.Brushing;
import com.kolibree.sdkws.data.model.CreateBrushingData;
import com.kolibree.sdkws.data.model.FacebookLoginData;
import com.kolibree.sdkws.data.model.LoginData;
import com.kolibree.sdkws.data.model.PhoneNumberData;
import com.kolibree.sdkws.data.model.gopirate.GoPirateDatastore;
import com.kolibree.sdkws.data.request.BetaData;
import com.kolibree.sdkws.data.request.CreateAccountData;
import com.kolibree.sdkws.data.request.UpdateAccountV3Data;
import com.kolibree.sdkws.internal.OfflineUpdateDatastore;
import com.kolibree.sdkws.networking.Response;
import com.kolibree.sdkws.profile.ProfileManager;
import com.kolibree.sdkws.profile.persistence.repo.ProfileRepository;
import com.kolibree.sdkws.room.MigrateAccountsFacade;
import com.kolibree.sdkws.sms.SmsToken;
import com.kolibree.sdkws.sms.data.AccountData;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.subjects.SingleSubject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import timber.log.Timber;

/** Created by miguelaragues on 27/12/17. */
@SuppressWarnings("KotlinInternalInJava")
public class KolibreeConnectorTest extends BaseUnitTest {

  private static final String DEFAULT_ACCESS_TOKEN = "dasdas";
  private static final String USER_AGENT = "TestUserAgent";

  private KolibreeConnector connector;

  @Mock private AccountInternal currentAccount;

  @Mock private SynchronizationScheduler synchronizationScheduler;

  @Mock private InternalAccountManager accountManager;

  @Mock private AccountDatastore accountDatastore;

  @Mock private ProfileManager profileManager;

  @Mock private GoPirateDatastore goPirateDatastore;

  @Mock private OfflineUpdateDatastore offlineUpdateDatastore;

  @Mock private BrushingsRepository brushingsRepository;

  @Mock private ProfileRepository profileRepository;

  @Mock private CurrentProfileProvider currentProfileProvider;

  @Mock private MigrateAccountsFacade migrateAccountsFacade;

  @Mock private BackendInteractor backendInteractor;

  @Mock private Gson gson;

  @Mock private InternalForceAppUpdater forceAppUpdater;

  @Mock private OnUserLoggedInCallback onUserLoggedInCallback;

  @Override
  public void setup() throws Exception {
    super.setup();
    defaultConnector();
  }

  /*
  INIT
   */
  @Test
  public void init_invokesMigrateAccountFacade() {
    verify(migrateAccountsFacade).maybeMigrateAccounts();
  }

  /*
  ADD BRUSHING
   */

  @Test(expected = IllegalStateException.class)
  public void addBrushingSync_emptyAccount_does_nothing() {
    CreateBrushingData data = mock(CreateBrushingData.class);
    ProfileInternal profile = mock(ProfileInternal.class);
    when(accountDatastore.getAccountMaybe()).thenReturn(Maybe.empty());

    connector.addBrushingSync(data, profile);

    verify(brushingsRepository, times(0)).addBrushing(any(), any(), anyLong());
  }

  @Test
  public void addBrushingSync_invokes_brushingRepository_addBrushing() {
    CreateBrushingData data = mock(CreateBrushingData.class);
    ProfileInternal profile = mock(ProfileInternal.class);
    long accountId = 12L;
    AccountInternal account = mock(AccountInternal.class);
    when(account.getId()).thenReturn(accountId);
    when(accountDatastore.getAccountMaybe()).thenReturn(Maybe.just(account));
    Brushing brushing = mock(Brushing.class);
    when(brushingsRepository.addBrushing(data, profile, accountId))
        .thenReturn(Single.just(brushing));

    connector.addBrushingSync(data, profile);

    verify(brushingsRepository).addBrushing(data, profile, accountId);
  }

  @Test
  public void addBrushingSingle_emptyAccount_does_nothing() {
    CreateBrushingData data = mock(CreateBrushingData.class);
    ProfileInternal profile = mock(ProfileInternal.class);
    when(accountDatastore.getAccountMaybe()).thenReturn(Maybe.empty());

    TestObserver<Brushing> observer = connector.addBrushingSingle(data, profile).test();

    observer.assertError(IllegalStateException.class);
    verify(brushingsRepository, times(0)).addBrushing(any(), any(), anyLong());
  }

  @Test
  public void addBrushingSingle_invokes_brushingRepository_addBrushing() {
    CreateBrushingData data = mock(CreateBrushingData.class);
    ProfileInternal profile = mock(ProfileInternal.class);
    long accountId = 12L;
    AccountInternal account = mock(AccountInternal.class);
    when(account.getId()).thenReturn(accountId);
    when(accountDatastore.getAccountMaybe()).thenReturn(Maybe.just(account));
    Brushing brushing = mock(Brushing.class);
    when(brushingsRepository.addBrushing(data, profile, accountId))
        .thenReturn(Single.just(brushing));

    TestObserver<Brushing> observer = connector.addBrushingSingle(data, profile).test();

    observer.assertValue(brushing);
    observer.assertNoErrors();
    verify(brushingsRepository).addBrushing(data, profile, accountId);
  }

  /*
  SEND REFRESH BROADCAST
   */

  @Test
  public void sendRefreshBroadcast_emitsCurrentProfileId() {
    TestObserver<Long> observer = connector.getRefreshObservable().test();

    observer.assertEmpty();

    Long expectedId = 9L;
    when(currentAccount.getCurrentProfileId()).thenReturn(expectedId);

    connector.sendRefreshBroadcast();

    observer.assertValue(expectedId);
  }

  /*
  LOGOUT
   */

  @Test
  public void logout_canAttemptToLogOutFalse_returnsCompletedCompletable() {
    doReturn(false).when(connector).canAttemptToLogout();

    connector.logout().test().assertNoErrors().assertComplete();
  }

  @Test
  public void logout_canAttemptToLogOutFalse_invokesOnLogoutCompleted() {
    doReturn(false).when(connector).canAttemptToLogout();

    connector.logout().test().assertNoErrors().assertComplete();

    verify(connector).onLogoutCompleted();
  }

  @Test
  public void logout_canAttemptToLogOutTrue_accountManagerLogoutError_emitsError() {
    doReturn(true).when(connector).canAttemptToLogout();

    Throwable expectedError = new Throwable("Test forced error");
    Completable logoutCompletable = Completable.error(expectedError);
    prepareLogout(logoutCompletable);

    connector.logout().test().assertError(expectedError);
  }

  @Test
  public void
      logout_canAttemptToLogOutTrue_accountManagerLogoutCompletes_invokesOnLogoutCompleted() {
    doReturn(true).when(connector).canAttemptToLogout();

    Completable logoutCompletable = Completable.complete();
    prepareLogout(logoutCompletable);

    doNothing().when(connector).onLogoutCompleted();

    connector.logout().test().assertComplete();

    verify(connector).onLogoutCompleted();
  }

  private void prepareLogout(Completable logoutCompletable) {
    long expectedAccountId = 9;
    doReturn(expectedAccountId).when(connector).getAccountId();
    String expectedRefreshToken = "refres";
    String expectedAccessToken = "access";
    doReturn(expectedRefreshToken).when(connector).getRefreshToken();
    doReturn(expectedAccessToken).when(connector).getAccessToken();
    when(accountManager.logout(expectedAccountId, expectedRefreshToken, expectedAccessToken))
        .thenReturn(logoutCompletable);
  }

  /*
  CAN ATTEMPT TO LOGOUT
   */

  @Test
  public void canAttemptToLogout_accountNull_returnsFalse() {
    currentAccountEmptyConnector();

    assertFalse(connector.canAttemptToLogout());
  }

  @Test
  public void canAttemptToLogout_accountNotNull_refreshTokenNull_returnsFalse() {
    doReturn(null).when(connector).getRefreshToken();

    assertFalse(connector.canAttemptToLogout());
  }

  @Test
  public void canAttemptToLogout_accountNotNull_refreshTokenNotNull_accessTokenNull_returnsFalse() {
    doReturn("token").when(connector).getRefreshToken();
    doReturn(null).when(connector).getAccessToken();

    assertFalse(connector.canAttemptToLogout());
  }

  @Test
  public void
      canAttemptToLogout_accountNotNull_refreshTokenNotNull_accessTokenNotNull_returnsTrue() {
    doReturn("token").when(connector).getRefreshToken();
    doReturn("access").when(connector).getAccessToken();

    assertTrue(connector.canAttemptToLogout());
  }

  /*
  ON LOGOUT COMPLETED
   */

  @Test
  public void onLogoutCompleted_invokesClearCurrentAccount() {
    assertEquals(currentAccount, connector.currentAccount());

    doNothing().when(connector).clearCurrentAccount();

    connector.onLogoutCompleted();

    verify(connector).clearCurrentAccount();
  }

  @Test
  public void onLogoutCompleted_invokesonUserLoggedInCallback() {
    doNothing().when(connector).clearCurrentAccount();

    connector.onLogoutCompleted();

    verify(onUserLoggedInCallback).onUserLoggedOut();
  }

  /*
  CLEAR CURRENT ACCOUNT
   */

  @Test
  public void clearCurrentAccount_truncatesAccountDatastore() {
    connector.clearCurrentAccount();

    verify(accountDatastore).truncate();
  }

  /*
  CREATE ANONYMOUS ACCOUNT
   */

  @Test
  public void createAnonymousAccount_invokesAccountManager() {
    CreateAccountData createV3Data = mock(CreateAccountData.class);

    when(accountManager.createAnonymousAccount(createV3Data)).thenReturn(SingleSubject.create());

    connector.createAnonymousAccount(createV3Data).test();

    verify(accountManager).createAnonymousAccount(createV3Data);
  }

  @Test
  public void createAnonymousAccount_invokessaveAccount() throws Exception {
    CreateAccountData createV3Data = mock(CreateAccountData.class);

    SingleSubject<AccountInternal> responseSingle = SingleSubject.create();
    when(accountManager.createAnonymousAccount(createV3Data)).thenReturn(responseSingle);

    doNothing().when(connector).saveAccount(any());

    connector.createAnonymousAccount(createV3Data).test();

    verify(connector, never()).saveAccount(any());

    AccountInternal accountInternal = mock(AccountInternal.class);
    responseSingle.onSuccess(accountInternal);

    verify(connector).saveAccount(accountInternal);
  }

  /*
  UPDATE ACCOUNT
   */

  @Test
  public void updateAccount_invokesAccountManager() {
    UpdateAccountV3Data updateV3Data = mock(UpdateAccountV3Data.class);
    long accountId = 54365L;

    when(accountManager.updateAccount(accountId, updateV3Data)).thenReturn(SingleSubject.create());

    connector.updateAccount(accountId, updateV3Data).test();

    verify(accountManager).updateAccount(accountId, updateV3Data);
  }

  /*
  UPDATE ACCOUNT WITH BETA VALUE
   */

  @Test
  public void updateAccountBeta_invokesAccountManager() {
    BetaData mockedBeta = mock(BetaData.class);

    when(accountManager.updateBetaAccount(243L, mockedBeta)).thenReturn(SingleSubject.create());

    connector.updateBetaAccount(243L, mockedBeta).test();

    verify(accountManager).updateBetaAccount(243L, mockedBeta);
  }

  @Test
  public void updateAccount_invokessaveAccount() {
    UpdateAccountV3Data updateV3Data = mock(UpdateAccountV3Data.class);
    long accountId = 54365L;

    SingleSubject<AccountInternal> responseSingle = SingleSubject.create();
    when(accountManager.updateAccount(accountId, updateV3Data)).thenReturn(responseSingle);

    doNothing().when(connector).saveAccount(any());

    connector.updateAccount(accountId, updateV3Data).test();

    verify(connector, never()).saveAccount(any());

    AccountInternal accountInternal = mock(AccountInternal.class);
    responseSingle.onSuccess(accountInternal);

    verify(connector).saveAccount(accountInternal);
  }

  /*
  SAVE ACCOUNT
   */

  @Test
  public void saveAccount_invokesMethodsInCorrectOrder() {
    AccountInternal account = mock(AccountInternal.class);

    doNothing().when(connector).preserveTokens(account);
    doNothing().when(connector).setCurrentAccount(account);

    InOrder inOrder = inOrder(connector, account);

    connector.saveAccount(account);

    inOrder.verify(connector).preserveTokens(account);
    inOrder.verify(connector).setCurrentAccount(account);
  }

  /*
  SET CURRENT ACCOUNT
   */
  @Test
  public void setCurrentAccount_invokesAccountDataStoreSetAccount() {
    AccountInternal account = mock(AccountInternal.class);

    connector.setCurrentAccount(account);

    verify(accountDatastore).setAccount(account);
  }

  /*
  PRESERVE LOGIN INFO
   */

  @Test
  public void preserveLoginInfo_incomingEmptyAccessToken_isAssignedAccessToken() {
    AccountInternal incomingAccount = mock(AccountInternal.class);
    when(incomingAccount.getAccessToken()).thenReturn("");
    when(incomingAccount.getRefreshToken()).thenReturn("");

    String expectedToken = "das";
    doReturn(expectedToken).when(connector).getAccessToken();

    connector.preserveTokens(incomingAccount);

    verify(incomingAccount).setAccessToken(expectedToken);
  }

  @Test
  public void preserveLoginInfo_incomingWithAccessToken_isAssignedAccessToken() {
    AccountInternal incomingAccount = mock(AccountInternal.class);
    when(incomingAccount.getAccessToken()).thenReturn("das");
    when(incomingAccount.getRefreshToken()).thenReturn("");

    connector.preserveTokens(incomingAccount);

    verify(incomingAccount, never()).setAccessToken(anyString());
  }

  @Test
  public void preserveLoginInfo_incomingEmptyRefreshToken_isAssignedRefreshToken() {
    AccountInternal incomingAccount = mock(AccountInternal.class);
    when(incomingAccount.getAccessToken()).thenReturn("");
    when(incomingAccount.getRefreshToken()).thenReturn("");

    String expectedToken = "das";
    doReturn(expectedToken).when(connector).getRefreshToken();

    connector.preserveTokens(incomingAccount);

    verify(incomingAccount).setRefreshToken(expectedToken);
  }

  @Test
  public void preserveLoginInfo_incomingWithRefreshToken_isAssignedRefreshToken() {
    AccountInternal incomingAccount = mock(AccountInternal.class);
    when(incomingAccount.getAccessToken()).thenReturn("");
    when(incomingAccount.getRefreshToken()).thenReturn("das");

    connector.preserveTokens(incomingAccount);

    verify(incomingAccount, never()).setRefreshToken(anyString());
  }

  /*
  CREATE ACCOUNT
   */

  @Test
  public void
      createAnonymousAccount_accountManagerReturnsBody_mapsResponseToSaveCreatedAccountAndCompletes() {
    CreateAccountData createV3Data = mock(CreateAccountData.class);

    AccountInternal accountInternal = mock(AccountInternal.class);

    SingleSubject<AccountInternal> responseSubject = SingleSubject.create();
    when(accountManager.createAnonymousAccount(createV3Data)).thenReturn(responseSubject);

    doNothing().when(connector).saveAccount(any());

    TestObserver<Void> observer = connector.createAnonymousAccount(createV3Data).test();

    verify(connector, never()).saveAccount(accountInternal);

    responseSubject.onSuccess(accountInternal);

    verify(connector).saveAccount(accountInternal);

    observer.assertComplete();
  }

  @Test
  public void sendSmsCode_returnsVerificationToken() {
    String phoneNumber = "+123123123";
    SingleSubject<VerificationTokenResponse> responseSubject = SingleSubject.create();
    when(accountManager.sendSmsCode(phoneNumber)).thenReturn(responseSubject);

    TestObserver<SmsToken> test = connector.sendSmsCodeTo(phoneNumber).test();
    String token = "T0k3n";
    SmsToken smsToken = new SmsToken(phoneNumber, token);
    VerificationTokenResponse response = new VerificationTokenResponse(token);
    responseSubject.onSuccess(response);
    test.assertValue(smsToken);
  }

  @Test
  public void createAccountBySms_saveAccount() {
    AccountInternal responseBody = mock(AccountInternal.class);

    SingleSubject<AccountInternal> responseSubject = SingleSubject.create();
    when(accountManager.createAccountBySms(any())).thenReturn(responseSubject);
    doNothing().when(connector).saveAccount(any());

    doReturn(Collections.emptyList()).when(connector).getProfileList();

    AccountData data =
        new AccountData(
            TrustedClock.getNowLocalDate(), Gender.MALE, Handedness.RIGHT_HANDED, "pl", "name");
    TestObserver<List<IProfile>> test =
        connector.createAccount(new SmsToken("phone", "token"), "code", data).test();
    responseSubject.onSuccess(responseBody);
    test.assertComplete();
    verify(connector).saveAccountCompletable(any());
  }

  @Test
  public void loginBySms_saveAccountAndCallsSyncNow() {
    String token = "token";
    String phone = "123-123-000";
    String code = "777000";
    PhoneNumberData data = new PhoneNumberData(phone, token, code);
    AccountInternal accountInternal = mock(AccountInternal.class);

    SingleSubject<AccountInternal> responseSubject = SingleSubject.create();
    when(accountManager.loginBySms(data)).thenReturn(responseSubject);
    doNothing().when(connector).saveAccount(any());

    doReturn(Collections.emptyList()).when(connector).getProfileList();

    TestObserver<List<IProfile>> test =
        connector.loginToAccount(new SmsToken(phone, token), code).test();
    responseSubject.onSuccess(accountInternal);
    test.assertComplete();
    verify(connector).saveAccountCompletable(any());
    verify(synchronizationScheduler).syncNow();
  }

  @Test
  public void saveAccountCompletable_invokesSaveAccount() {
    AccountInternal accountInternal = mock(AccountInternal.class);
    doNothing().when(connector).saveAccount(any());

    connector.saveAccountCompletable(Single.just(accountInternal)).test();
    verify(connector).saveAccount(any());
  }

  @Test
  public void createEmailAccount_invokessaveAccount() {
    AccountInternal accountInternal = mock(AccountInternal.class);
    doNothing().when(connector).saveAccount(accountInternal);
    CreateAccountData data = mock(CreateAccountData.class);
    when(accountManager.createEmailAccount(data)).thenReturn(Single.just(accountInternal));

    connector.createEmailAccount(data).test();
    verify(connector).saveAccount(accountInternal);
  }

  @Test
  public void createEmailAccount_invokesSyncNow() {
    AccountInternal accountInternal = mock(AccountInternal.class);
    doNothing().when(connector).saveAccount(accountInternal);
    CreateAccountData data = mock(CreateAccountData.class);
    when(accountManager.createEmailAccount(data)).thenReturn(Single.just(accountInternal));

    connector.createEmailAccount(data).test();

    verify(synchronizationScheduler).syncNow();
  }

  /*
  ACCOUNT HAS PASSWORD
   */

  @Test
  public void testCreateAccount_serverErrorEmitsError() {
    mockConnectorAccount();

    Throwable t = new Throwable();
    when(accountManager.createAccount(any(), any())).thenReturn(Completable.error(t));

    connector.createAccountByEmail("my_email").test().assertError(t);
  }

  @Test
  public void testCreateAccount_successEmitsComplete() {
    AccountInternal account = mockConnectorAccount();

    when(accountManager.createAccount(any(), any())).thenReturn(Completable.complete());

    connector.createAccountByEmail("my_email").test().assertNoErrors().assertComplete();
    verify(accountDatastore).updateEmail(account);
  }

  @Test
  public void testDeleteAccount_successEmitsComplete() {
    AccountInternal account = mockConnectorAccount();

    when(accountManager.deleteAccount(account.getId())).thenReturn(Completable.complete());

    connector.deleteAccount().test().assertNoErrors().assertComplete();
  }

  @Test
  public void testUpdateWeeklydigest_successEmitsComplete() {
    AccountInternal account = mockConnectorAccount();

    when(accountManager.weeklyDigest(account.getId(), true)).thenReturn(Completable.complete());

    connector.enableWeeklyDigest(true).test().assertNoErrors().assertComplete();
    verify(accountDatastore).updateAllowDigest(account);
  }

  @Test
  public void testAccountHasPassword_oldAccount() {
    final String errorMessage = "failed";
    final Response response = mockErrorResponse(errorMessage, 25);
    when(backendInteractor.call(any(Request.class), any())).thenReturn(response);

    final TestObserver<Boolean> testObserver = connector.accountHasPassword("toto").test();
    testObserver.assertNoErrors();
    testObserver.assertValue(true);
  }

  @Test
  public void testAccountHasPassword_magicLinkAccount() {
    final String errorMessage = "failed";
    final Response response = mockErrorResponse(errorMessage, 326);
    when(backendInteractor.call(any(Request.class), any())).thenReturn(response);

    final TestObserver<Boolean> testObserver = connector.accountHasPassword("toto").test();
    testObserver.assertNoErrors();
    testObserver.assertValue(false);
  }

  @Test
  public void testAccountHasPassword_error() {
    final String errorMessage = "Unknown email";
    final Response errorResponse = mockErrorResponse(errorMessage, 1);

    when(backendInteractor.call(any(Request.class), any())).thenReturn(errorResponse);

    final TestObserver testObserver = connector.accountHasPassword("toto").test();
    testObserver.assertErrorMessage(errorMessage);
  }

  /** GOOGLE LOGIN */
  @Test
  public void loginByGoogle_success() {
    String accessToken = "ACCESS_TOKEN";
    String refreshToken = "REFRESH_TOKEN";
    AccountInternal account = mockConnectorAccount();
    when(account.getAccessToken()).thenReturn(accessToken);
    when(account.getRefreshToken()).thenReturn(refreshToken);

    CreateAccountData data = CreateAccountData.builder().build();
    when(accountManager.loginByGoogle(data)).thenReturn(Single.just(account));

    final TestObserver testObserver = connector.loginByGoogle(data).test();

    testObserver.assertNoErrors();
    testObserver.assertComplete();

    verify(connector).saveAccount(account);
    verify(synchronizationScheduler).syncNow();
  }

  @Test
  public void loginByGoogle_error() {
    Exception exception = new RuntimeException("Registration failed");
    CreateAccountData data = CreateAccountData.builder().build();
    when(accountManager.loginByGoogle(data)).thenReturn(Single.error(exception));

    final TestObserver testObserver = connector.loginByGoogle(data).test();

    testObserver.assertError(exception);

    verify(connector, never()).saveAccount(any());
    verify(synchronizationScheduler, never()).syncNow();
  }

  /*
  GOOGLE SIGN UP
  */

  @Test
  public void createAccountByGoogle_success() {
    String accessToken = "ACCESS_TOKEN";
    String refreshToken = "REFRESH_TOKEN";
    AccountInternal account = mockConnectorAccount();
    when(account.getAccessToken()).thenReturn(accessToken);
    when(account.getRefreshToken()).thenReturn(refreshToken);

    CreateAccountData data = CreateAccountData.builder().build();
    when(accountManager.registerWithGoogle(data)).thenReturn(Single.just(account));

    final TestObserver testObserver = connector.createAccountByGoogle(data).test();

    testObserver.assertNoErrors();
    testObserver.assertComplete();

    verify(connector).saveAccount(account);
    verify(connector, never()).maybeUploadGoogleAvatar(eq(account), anyString());
    verify(synchronizationScheduler).syncNow();
  }

  @Test
  public void createAccountByGoogle_successWithAvatar() {
    String accessToken = "ACCESS_TOKEN";
    String refreshToken = "REFRESH_TOKEN";
    String avatarUrl = "http://example.com/picture.jpg";
    AccountInternal account = mockConnectorAccount();
    when(account.getAccessToken()).thenReturn(accessToken);
    when(account.getRefreshToken()).thenReturn(refreshToken);

    CreateAccountData data = CreateAccountData.builder().setGoogleAvatarUrl(avatarUrl).build();
    when(accountManager.registerWithGoogle(data)).thenReturn(Single.just(account));

    final TestObserver testObserver = connector.createAccountByGoogle(data).test();

    testObserver.assertNoErrors();
    testObserver.assertComplete();

    verify(connector).maybeUploadGoogleAvatar(account, avatarUrl);
  }

  @Test
  public void createAccountByGoogle_error() {
    Exception exception = new RuntimeException("Registration failed");
    CreateAccountData data = CreateAccountData.builder().build();
    when(accountManager.registerWithGoogle(data)).thenReturn(Single.error(exception));

    final TestObserver testObserver = connector.createAccountByGoogle(data).test();

    testObserver.assertError(exception);

    verify(connector, never()).saveAccount(any());
    verify(connector, never()).maybeUploadGoogleAvatar(any(), anyString());
    verify(synchronizationScheduler, never()).syncNow();
  }

  /*
  maybeUploadGoogleAvatar
  */

  @Test
  public void maybeUploadGoogleAvatar_happyPath() {
    String avatarUrl = "http://example.com/picture.jpg";
    ProfileInternal ownerProfile = BrushingSharedHelperKt.createProfile();

    AccountInternal account = mockConnectorAccount();
    when(account.getOwnerProfileId()).thenReturn(ownerProfile.getId());
    when(account.getProfileInternalWithId(ownerProfile.getId())).thenReturn(ownerProfile);

    final TestObserver<AccountInternal> testObserver =
        connector.maybeUploadGoogleAvatar(account, avatarUrl).test();

    testObserver.assertNoErrors();
    testObserver.assertValue(account);
    testObserver.assertComplete();

    verify(profileManager).downloadExternalPicture(any(), eq(avatarUrl));
    verify(connector).currentAccount();
  }

  @Test
  public void maybeUploadGoogleAvatar_errorUploadRecoversWithCurrentAccount() {
    String avatarUrl = "http://example.com/picture.jpg";
    ProfileInternal ownerProfile = BrushingSharedHelperKt.createProfile();

    AccountInternal account = mockConnectorAccount();
    when(account.getOwnerProfileId()).thenReturn(ownerProfile.getId());
    when(account.getProfileInternalWithId(ownerProfile.getId())).thenReturn(ownerProfile);
    when(profileManager.downloadExternalPicture(any(), eq(avatarUrl)))
        .thenReturn(Single.error(new RuntimeException("Upload failed!")));

    final TestObserver<AccountInternal> testObserver =
        connector.maybeUploadGoogleAvatar(account, avatarUrl).test();

    testObserver.assertNoErrors();
    testObserver.assertValue(account);
    testObserver.assertComplete();

    verify(connector).currentAccount();
  }

  /*
  doLogin
   */

  @Test
  public void doLogin_emitsTrue_whenResponseIsSuccessfulAndAllOperationsSucceed() {
    Request request = prepareDoLoginSuccessScenario();

    connector.doLogin(request).test().assertValue(true);
  }

  @Test
  public void doLogin_invokesSaveAccount_whenResponseIsSuccessful() {
    AccountInternal accountInternal = mock(AccountInternal.class);
    Request request = prepareDoLoginSuccessScenario(accountInternal);

    TestObserver observer = connector.doLogin(request).test().assertValue(true);

    verify(connector).saveAccount(accountInternal);
  }

  @Test
  public void doLogin_invokesNotifySuccessfulLogin_whenResponseIsSuccessful() {
    Request request = prepareDoLoginSuccessScenario();

    connector.doLogin(request).test();

    verify(connector).notifySuccessfulLogin();
  }

  @Test
  public void doLogin_invokesSyncNow_whenResponseIsSuccessful() {
    Request request = prepareDoLoginSuccessScenario();

    connector.doLogin(request).test();

    verify(synchronizationScheduler).syncNow();
  }

  @Test
  public void doLogin_responseErrorWithCodeCODE_NEED_ADDITIONAL_DATA_emitsFalse() {
    Response response = mockErrorResponse("", ACCOUNT_ADDITIONAL_INFO_NEEDED);
    Request request = prepareDoLoginScenario(response);

    connector.doLogin(request).test().assertValue(false);
  }

  @Test
  public void doLogin_responseErrorWithOtherCode_emitsError() {
    Response response = mockErrorResponse("", ApiErrorCode.UNKNOWN_ERROR);
    Request request = prepareDoLoginScenario(response);

    TestObserver<Boolean> observer = connector.doLogin(request).test();

    ApiError error = (ApiError) observer.assertError(ApiError.class).errors().get(0);
    assertEquals(ApiErrorCode.UNKNOWN_ERROR, error.getInternalErrorCode());
  }

  /*
  syncAndNotify
   */

  @Test
  public void syncAndNotify_currentAccountIsNull_emitsFalse() {
    doReturn(null).when(connector).currentAccount();

    connector.syncAndNotify().test().assertValue(false);
  }

  @Test
  public void syncAndNotify_currentAccountIsNotNullNull_emitsTrue() {
    AccountInternal expectedAccount = mock(AccountInternal.class);
    doReturn(expectedAccount).when(connector).currentAccount();

    doNothing().when(connector).setCurrentAccount(any());

    connector.syncAndNotify().test().assertValue(true);

    verify(connector).setCurrentAccount(expectedAccount);
  }

  @Test
  public void syncAndNotify_currentAccountIsNullOnSecondInvocation_emitsFalse() {
    AccountInternal expectedAccount = mock(AccountInternal.class);
    AccountInternal nullAccount = null;
    //noinspection ConstantConditions
    doReturn(expectedAccount, nullAccount).when(connector).currentAccount();

    doNothing().when(connector).setCurrentAccount(any());

    connector.syncAndNotify().test().assertValue(false);

    verify(connector).setCurrentAccount(expectedAccount);
  }

  @Test
  public void syncAndNotify_currentAccountIsNotNullNull_invokesSetCurrentAccount() {
    AccountInternal expectedAccount = mock(AccountInternal.class);
    doReturn(expectedAccount).when(connector).currentAccount();

    doNothing().when(connector).setCurrentAccount(any());

    connector.syncAndNotify().test();

    verify(connector).setCurrentAccount(expectedAccount);
  }

  @Test
  public void syncAndNotify_currentAccountIsNotNullNull_invokesNotifySuccessfulLogin() {
    AccountInternal expectedAccount = mock(AccountInternal.class);
    doReturn(expectedAccount).when(connector).currentAccount();

    doNothing().when(connector).setCurrentAccount(any());

    connector.syncAndNotify().test();

    verify(connector).notifySuccessfulLogin();
  }

  @Test
  public void syncAndNotify_currentAccountIsNotNullNull_invokesSyncNow() {
    AccountInternal expectedAccount = mock(AccountInternal.class);
    doReturn(expectedAccount).when(connector).currentAccount();

    doNothing().when(connector).setCurrentAccount(any());

    connector.syncAndNotify().test();

    verify(synchronizationScheduler).syncNow();
  }

  /*
  notifySuccessfulLogin
   */

  @Test
  public void notifySuccessfulLogin_invokesOnUserLoggedIn_ifCallbackWasRegistered() {
    connector.notifySuccessfulLogin();

    verify(onUserLoggedInCallback).onUserLoggedIn();
  }

  /*
  login
   */

  @Test
  public void testLogin_emailPassword_noErrorAndCallsSyncNow() {
    when(backendInteractor.call(any(Request.class), any())).thenReturn(new Response(200, ""));
    doNothing().when(connector).saveAccount(any());

    final TestObserver testObserver = connector.login(new LoginData("email", "password")).test();

    testObserver.assertNoErrors();
    testObserver.assertComplete();
    verify(synchronizationScheduler).syncNow();
  }

  @Test
  public void testLogin_emailPassword_error() {
    final String errorMessage = "Invalid password";
    final Response response = mockErrorResponse(errorMessage, 1);
    when(backendInteractor.call(any(Request.class), any())).thenReturn(response);

    final TestObserver testObserver = connector.login(new LoginData("email", "password")).test();

    testObserver.assertErrorMessage(errorMessage);
    verify(synchronizationScheduler, never()).syncNow();
  }

  @Test
  public void testLogin_facebookLogin_noErrorAndCallsSyncNow() {
    doReturn(Single.just(true)).when(connector).doLogin(any());

    final TestObserver<Boolean> testObserver =
        connector.login(new FacebookLoginData("1234", "muy_buenas_tardes")).test();

    testObserver.assertNoErrors();
    testObserver.assertValue(true);
    verify(synchronizationScheduler, never()).syncNow();
  }

  @Test
  public void testLogin_facebookSignUp_noError() {
    doReturn(Single.just(false)).when(connector).doLogin(any());

    final TestObserver<Boolean> testObserver =
        connector.login(new FacebookLoginData("1234", "muy_buenas_tardes")).test();

    testObserver.assertNoErrors();
    testObserver.assertValue(false);
    verify(synchronizationScheduler, never()).syncNow();
  }

  @Test
  public void testLogin_facebookLogin_error() {
    final String errorMessage = "Invalid password";
    final Response response = mockErrorResponse(errorMessage, 1);
    when(backendInteractor.call(any(Request.class), any())).thenReturn(response);

    final TestObserver testObserver = connector.login(new LoginData("email", "password")).test();

    testObserver.assertErrorMessage(errorMessage);
    verify(synchronizationScheduler, never()).syncNow();
  }

  @Test
  public void testLogin_magicLink_noErrorAndCallsSyncNow() {
    doReturn(Single.just(true)).when(connector).doLogin(any());

    final TestObserver testObserver = connector.login("magicCode").test();

    testObserver.assertNoErrors();
    testObserver.assertComplete();
    verify(synchronizationScheduler, never()).syncNow();
  }

  @Test
  public void testLogin_magicLink_error() {
    final String errorMessage = "Expired magic link code";
    final Response response = mockErrorResponse(errorMessage, 1);
    when(backendInteractor.call(any(Request.class), any())).thenReturn(response);

    final TestObserver testObserver = connector.login("expiredCode").test();

    testObserver.assertErrorMessage(errorMessage);
    verify(synchronizationScheduler, never()).syncNow();
  }

  @Test
  public void testValidateMagicLinkCode_error() {
    final String errorMessage = "Expired code";
    final Response response = mockErrorResponse(errorMessage, 1);
    when(backendInteractor.call(any(Request.class), any())).thenReturn(response);

    final TestObserver<String> testObserver = connector.validateMagicLinkCode("code").test();

    testObserver.assertErrorMessage(errorMessage);
  }

  @Test
  public void testValidateMagicLinkCode_noError() {
    final String validatedCode = "jeSuisValidé";
    final Response response = mock(Response.class);
    when(response.getBody()).thenReturn("{\"code\":\"" + validatedCode + "\"}");
    when(response.succeeded()).thenReturn(true);
    when(backendInteractor.call(any(Request.class), any())).thenReturn(response);

    final TestObserver<String> testObserver = connector.validateMagicLinkCode("code").test();
    testObserver.assertNoErrors();
    testObserver.assertValue(validatedCode);
  }

  @Test
  public void testRequestMagicLink_noError() {
    final Response response = mock(Response.class);
    when(response.succeeded()).thenReturn(true);
    when(backendInteractor.call(any(Request.class), any())).thenReturn(response);

    final TestObserver testObserver = connector.requestMagicLink("email").test();

    testObserver.assertNoErrors();
    testObserver.assertComplete();
  }

  @Test
  public void testRequestMagicLink_error() {
    final String errorMessage = "Invalid email";
    final Response response = mockErrorResponse(errorMessage, 1);
    when(backendInteractor.call(any(Request.class), any())).thenReturn(response);

    final TestObserver testObserver = connector.requestMagicLink("email").test();

    testObserver.assertErrorMessage(errorMessage);
  }

  /*
  GET CURRENT PROFILE FLOWABLE
   */
  @Test
  public void currentProfileFlowable_returnsCurrentProfileProvider() {
    PublishProcessor<Profile> expectedFlowable = PublishProcessor.create();
    when(currentProfileProvider.currentProfileFlowable()).thenReturn(expectedFlowable);

    assertEquals(expectedFlowable, connector.currentProfileFlowable());
  }

  /*
  GET LIST PROFILES SINGLE
   */
  @Test
  public void getListProfilesSingle_hasConnectedAccountFalse_emitsError() {
    doReturn(false).when(connector).hasConnectedAccount();

    connector.getProfileListSingle().test().assertError(IllegalStateException.class);
  }

  @Test
  public void getListProfilesSingle_hasConnectedAccountTrue_profileManagerEmitsError_emitsError() {
    long accountId = 9L;
    doReturn(accountId).when(connector).getAccountId();

    Throwable expectedException = new Throwable("Test forced error");
    when(profileManager.getProfilesLocally()).thenReturn(Single.error(expectedException));

    connector.getProfileListSingle().test().assertError(expectedException);
  }

  @Test
  public void getListProfilesSingle_hasConnectedAccountTrue_profileManagerEmitsList_emitsList() {
    long accountId = 9L;
    doReturn(accountId).when(connector).getAccountId();
    List<Profile> expectedList = Arrays.asList(mock(Profile.class), mock(Profile.class));

    when(profileManager.getProfilesLocally()).thenReturn(Single.just(expectedList));

    connector.getProfileListSingle().test().assertValue(expectedList);
  }

  /*
  GET LIST PROFILES
   */
  @Test
  public void getListProfiles_getProfilesListEmitsError_returnsEmptyList() {
    doReturn(Single.error(new NoSuchElementException())).when(connector).getProfileListSingle();

    assertTrue(connector.getProfileList().isEmpty());
  }

  @Test
  public void getListProfiles_getProfilesListEmitsEmptyList_returnsEmptyList() {
    doReturn(Single.just(Collections.emptyList())).when(connector).getProfileListSingle();

    assertTrue(connector.getProfileList().isEmpty());
  }

  @Test
  public void getListProfiles_getProfilesListEmitsList_returnsList() {
    List<Profile> expectedList = Arrays.asList(mock(Profile.class), mock(Profile.class));
    doReturn(Single.just(expectedList)).when(connector).getProfileListSingle();

    assertEquals(expectedList, connector.getProfileList());
  }

  /*
  GET CURRENT PROFILE
   */
  @Test
  public void getCurrentProfile_noAccount_returnsNull() {
    doReturn(null).when(connector).currentAccount();

    assertNull(connector.getCurrentProfile());
  }

  @Test
  public void getCurrentProfile_getCurrentProfileIdReturnsNull_returnsOwnerProfile() {
    AccountInternal accountInternal = mock(AccountInternal.class);

    long ownerProfileId = 565;

    when(accountInternal.getCurrentProfileId()).thenReturn(null);
    doAnswer(
            (Answer<Void>)
                invocation -> {
                  when(accountInternal.getCurrentProfileId()).thenReturn(ownerProfileId);

                  return null;
                })
        .when(accountInternal)
        .setOwnerProfileAsCurrent();

    doReturn(accountInternal).when(connector).currentAccount();

    Profile expectedProfile = mock(Profile.class);
    doReturn(expectedProfile).when(connector).getProfileWithId(ownerProfileId);

    assertEquals(expectedProfile, connector.getCurrentProfile());
  }

  @Test
  public void getCurrentProfile_getCurrentProfileIdReturnsNull_invokesSetActiveProfile() {
    AccountInternal accountInternal = mock(AccountInternal.class);

    long ownerProfileId = 565;

    when(accountInternal.getCurrentProfileId()).thenReturn(null);
    when(accountInternal.getOwnerProfileId()).thenReturn(ownerProfileId);
    doAnswer(
            (Answer<Void>)
                invocation -> {
                  when(accountInternal.getCurrentProfileId()).thenReturn(ownerProfileId);

                  return null;
                })
        .when(accountInternal)
        .setOwnerProfileAsCurrent();

    doReturn(accountInternal).when(connector).currentAccount();

    doNothing().when(connector).setActiveProfile(ownerProfileId);

    Profile expectedProfile = mock(Profile.class);
    doReturn(expectedProfile).when(connector).getProfileWithId(ownerProfileId);

    connector.getCurrentProfile();

    verify(connector).setActiveProfile(ownerProfileId);
  }

  @Test
  public void getCurrentProfile_withAccountAndCurrentProfileId_returnsGetProfileWithId() {
    AccountInternal accountInternal = mock(AccountInternal.class);
    long currentProfileId = 9L;
    when(accountInternal.getCurrentProfileId()).thenReturn(currentProfileId);
    doReturn(accountInternal).when(connector).currentAccount();

    Profile expectedProfile = mock(Profile.class);
    doReturn(expectedProfile).when(connector).getProfileWithId(currentProfileId);

    assertEquals(expectedProfile, connector.getCurrentProfile());
  }

  @Test
  public void currentProfile_recoversFromDBWithCurrentProfileIdNull() {
    AccountInternal account = setupAccountWithCurrentProfileIdNull();
    assertNull(account.getCurrentProfileId());
    when(accountDatastore.getAccountMaybe()).thenReturn(Maybe.just(account));

    when(profileManager.getProfileLocally(account.getOwnerProfileId()))
        .thenReturn(Single.just(account.getOwnerProfile()));

    assertEquals(account.getOwnerProfile(), connector.getCurrentProfile());

    ArgumentCaptor<AccountInternal> captor = ArgumentCaptor.forClass(AccountInternal.class);
    verify(accountDatastore).updateCurrentProfileId(captor.capture());

    AccountInternal insertedAccount = captor.getValue();
    Long profileId = insertedAccount.getCurrentProfileId();
    assertNotNull(profileId);

    assertEquals(account.getOwnerProfileId(), (long) profileId);
  }

  @Test
  public void getCurrentProfile_invokesSanitizeCurrentProfile() {
    doNothing().when(connector).sanitizeCurrentProfile();

    connector.getCurrentProfile();

    verify(connector).sanitizeCurrentProfile();
  }

  /*
  GET OWNER PROFILE
   */
  @Test
  public void getOwnerProfile_noAccount_returnsNull() {
    doReturn(null).when(connector).currentAccount();

    assertNull(connector.getOwnerProfile());
  }

  @Test
  public void getOwnerProfile_accountThrowsIllegalStateException_returnsNull() {
    AccountInternal accountInternal = mock(AccountInternal.class);
    when(accountInternal.getOwnerProfile()).thenThrow(IllegalStateException.class);
    doReturn(accountInternal).when(connector).currentAccount();

    assertNull(connector.getOwnerProfile());
  }

  @Test
  public void getOwnerProfile_withAccount_returnsGetProfileWithId() {
    AccountInternal accountInternal = mock(AccountInternal.class);
    long ownerProfileId = 9L;
    Profile ownerProfile = mock(Profile.class);
    when(ownerProfile.getId()).thenReturn(ownerProfileId);
    when(accountInternal.getOwnerProfile()).thenReturn(ownerProfile);
    doReturn(accountInternal).when(connector).currentAccount();

    assertEquals(ownerProfile, connector.getOwnerProfile());
  }

  /*
  GET PROFILE WITH ID SINGLE
   */
  @Test
  public void getProfileWithIdSingle_profileManagerEmitsError_emitsError() {
    long profileId = 8;

    Throwable expectedError = new NoSuchElementException();
    when(profileManager.getProfileLocally(profileId)).thenReturn(Single.error(expectedError));

    connector.getProfileWithIdSingle(profileId).test().assertError(expectedError);
  }

  @Test
  public void getProfileWithIdSingle_profileManagerEmitsProfile_emitsProfile() {
    long profileId = 8;

    Profile expectedProfile = mock(Profile.class);
    when(profileManager.getProfileLocally(profileId)).thenReturn(Single.just(expectedProfile));

    connector.getProfileWithIdSingle(profileId).test().assertValue(expectedProfile);
  }

  /*
  GET PROFILE WITH ID
   */
  @Test
  public void getProfileWithId_getProfileWithIdEmitsError_returnsNull() {
    long profileId = 8;

    doReturn(Single.error(new NoSuchElementException()))
        .when(connector)
        .getProfileWithIdSingle(profileId);

    assertNull(connector.getProfileWithId(profileId));
  }

  @Test
  public void getProfileWithId_getProfileWithIdEmitsProfile_returnsProfile() {
    long profileId = 8;
    Profile expectedProfile = mock(Profile.class);

    doReturn(Single.just(expectedProfile)).when(connector).getProfileWithIdSingle(profileId);

    assertEquals(expectedProfile, connector.getProfileWithId(profileId));
  }

  /*
  NEEDS PARENTAL CONSENT
   */
  @Test
  public void needsParentalConsent_response_204_nullBody_returnsFalse() {
    successfulResponse(204, null);

    connector.needsParentalConsent(TrustedClock.getNowLocalDate()).test().assertValue(false);
  }

  /*
  SYNCHRONIZE BRUSHINGS
   */
  @Test
  public void
      synchronizeBrushing_brushingRepositorySynchronizeBrushingReturnsFalse_returnsCompletedCompletable() {
    long accountId = 67L;
    doReturn(accountId).when(connector).getAccountId();

    Single<Boolean> expectedCompletable = Single.just(false);

    long profileId = 4L;

    when(brushingsRepository.synchronizeBrushing(accountId, profileId))
        .thenReturn(expectedCompletable);

    connector.synchronizeBrushing(profileId).test().assertComplete();
  }

  @Test
  public void
      synchronizeBrushing_brushingRepositorySynchronizeBrushingReturnsTrue_returnsCompletedCompletable() {
    long accountId = 67L;
    doReturn(accountId).when(connector).getAccountId();

    Single<Boolean> expectedCompletable = Single.just(true);

    long profileId = 4L;

    when(brushingsRepository.synchronizeBrushing(accountId, profileId))
        .thenReturn(expectedCompletable);

    connector.synchronizeBrushing(profileId).test().assertComplete();
  }

  /*
  NEEDS PARENTAL CONSENT
   */

  @Test
  public void setActiveProfileId_currentAccountNull_doesNothing() {
    setConnectorAccount(null);

    assertNull(connector.currentAccount());

    connector.setActiveProfile(1);
  }

  @Test
  public void setActiveProfileId_accountNotNull_setsActiveProfileInAccount() {
    assertNotNull(connector.currentAccount());

    long expectedActiveProfile = 56L;
    connector.setActiveProfile(expectedActiveProfile);

    verify(connector.currentAccount()).setCurrentProfileId(expectedActiveProfile);
  }

  @Test
  public void setActiveProfileId_accountNotNull_updatesAccountDataStore() {
    assertNotNull(connector.currentAccount());

    connector.setActiveProfile(1);

    verify(accountDatastore).updateCurrentProfileId(currentAccount);
  }

  @Test
  public void setActiveProfileId_accountNotNull_sendsRefreshBroadcast() {
    assertNotNull(connector.currentAccount());

    TestObserver<Long> observer = connector.getRefreshObservable().test();

    observer.assertValueCount(0);

    connector.setActiveProfile(1);

    observer.assertValueCount(1);
  }

  /*
  setActiveProfileCompletable
   */

  @Test
  public void setActiveProfileCompletable_nullAccount_completesDoingNothing() {
    setConnectorAccount(null);
    final TestObserver<Long> refreshObservable = connector.getRefreshObservable().test();

    connector.setActiveProfileCompletable(1L).test().assertComplete().assertNoErrors();

    verify(accountDatastore, never()).updateCurrentProfileId(any());
    refreshObservable.assertNoValues();
  }

  @Test
  public void setActiveProfileCompletable_nonNullAccount_updatesAndPersistsIt() {
    final long expectedProfileId = 1986L;
    final AccountInternal account = mock(AccountInternal.class);
    setConnectorAccount(account);

    connector
        .setActiveProfileCompletable(expectedProfileId)
        .test()
        .assertComplete()
        .assertNoErrors();

    verify(account).setCurrentProfileId(expectedProfileId);
    verify(accountDatastore).updateCurrentProfileId(account);
  }

  @Test
  public void setActiveProfileCompletable_nonNullAccount_sendsRefreshBroadcast() {
    final long expectedProfileId = 1986L;
    final AccountInternal account = mock(AccountInternal.class);
    setConnectorAccount(account);
    final TestObserver<Long> refreshObservable = connector.getRefreshObservable().test();

    connector
        .setActiveProfileCompletable(expectedProfileId)
        .test()
        .assertComplete()
        .assertNoErrors();

    refreshObservable.assertValueCount(1);
  }

  /*
  currentAccountOptionalSingle
   */

  @Test
  public void currentAccountOptionalSingle_nullAccount_emitsAbsent() {
    setConnectorAccount(null);

    connector
        .currentAccountOptionalSingle()
        .test()
        .assertComplete()
        .assertNoErrors()
        .assertValue(Optional.absent());
  }

  @Test
  public void currentAccountOptionalSingle_nonNullAccount_emitsCurrentAccount() {
    final AccountInternal expectedAccount = mock(AccountInternal.class);
    setConnectorAccount(expectedAccount);

    connector
        .currentAccountOptionalSingle()
        .test()
        .assertComplete()
        .assertNoErrors()
        .assertValue(Optional.of(expectedAccount));
  }

  /*
  NEEDS PARENTAL CONSENT
   */

  @Test
  public void needsParentalConsent_response_400_withBody_returnsTrue() {
    failureResponse(
        400,
        "{\"display_message\":\"Parental consent required\",\"message\":\"E416: parental consent required\",\"internal_error_code\":416,\"detail\":\"Parental consent required\",\"http_code\":400}");

    connector.needsParentalConsent(TrustedClock.getNowLocalDate()).test().assertValue(true);
  }

  @Test
  public void needsParentalConsent_response_401_emitsError() {
    failureResponse(
        401,
        "{\n"
            + "    \"display_message\": \"Wrong request (wrong signature)\",\n"
            + "    \"message\": \"E03: Need valid client credentials.\",\n"
            + "    \"internal_error_code\": 3,\n"
            + "    \"detail\": \"Wrong signature.\",\n"
            + "    \"http_code\": 401\n"
            + "}");

    connector
        .needsParentalConsent(TrustedClock.getNowLocalDate())
        .test()
        .assertError(Exception.class);
  }

  /*
  UTILS
   */

  private AccountInternal setupAccountWithCurrentProfileIdNull() {
    AccountInternal account = new AccountInternal();

    ProfileInternal profile = BrushingSharedHelperKt.createProfile();
    account.setInternalProfiles(Collections.singletonList(profile));
    account.setOwnerProfileId(profile.getId());

    account.setCurrentProfileId(null);

    return account;
  }

  @Test
  public void checkNeedsParentalConsent_response_200_emitsError() {
    successfulResponse("");

    connector
        .needsParentalConsent(TrustedClock.getNowLocalDate())
        .test()
        .assertError(Exception.class);
  }

  @Test
  public void checkNeedsParentalConsentStatus_no_current_account_returnUNKNONW() {
    doReturn(null).when(connector).currentAccount();

    assertEquals(ParentalConsent.UNKNOWN, connector.parentalConsentStatus());
  }

  /*
  sanitizeCurrentProfile
   */

  @Test
  public void sanitizeCurrentProfile_currentProfileNotExisting_resetsIt() {
    final long currentProfileId = 1986L;
    final long ownerProfileId = 1983L;
    final AccountInternal account = mock(AccountInternal.class);
    when(account.getCurrentProfileId()).thenReturn(currentProfileId);
    when(account.getOwnerProfileId()).thenReturn(ownerProfileId);
    when(account.knows(currentProfileId)).thenReturn(false);
    setConnectorAccount(account);

    connector.sanitizeCurrentProfile();

    verify(account).setOwnerProfileAsCurrent();
    verify(connector).setActiveProfile(ownerProfileId);
  }

  @Test
  public void sanitizeCurrentProfile_currentProfileExisting_doesNothing() {
    final long currentProfileId = 1986L;
    final AccountInternal account = mock(AccountInternal.class);
    when(account.getCurrentProfileId()).thenReturn(currentProfileId);
    when(account.knows(currentProfileId)).thenReturn(true);
    setConnectorAccount(account);

    connector.sanitizeCurrentProfile();

    verify(account, never()).setOwnerProfileAsCurrent();
    verify(connector, never()).setActiveProfile(anyLong());
  }

  private Response successfulResponse(String body) {
    return successfulResponse(200, body);
  }

  private Response successfulResponse(int httpCode, String body) {
    Response response = mock(Response.class);

    when(response.getBody()).thenReturn(body);
    when(response.succeeded()).thenReturn(true);
    when(response.getHttpCode()).thenReturn(httpCode);

    return mockCallScenario(response);
  }

  private Response failureResponse(int errorCode, String message) {
    Response response = mock(Response.class);

    when(response.succeeded()).thenReturn(false);
    when(response.getHttpCode()).thenReturn(errorCode);
    ApiError apiError = mock(ApiError.class);
    when(apiError.getMessage()).thenReturn(message);
    when(response.getError()).thenReturn(apiError);

    return mockCallScenario(response);
  }

  private Response mockCallScenario(Response response) {
    when(currentAccount.getAccessToken()).thenReturn(DEFAULT_ACCESS_TOKEN);

    when(backendInteractor.call(any(Request.class), anyString())).thenReturn(response);

    return response;
  }

  private AccountInternal mockConnectorAccount() {
    AccountInternal accountInternal = mock(AccountInternal.class);
    long currentProfileId = 9L;
    when(accountInternal.getCurrentProfileId()).thenReturn(currentProfileId);
    when(accountInternal.getAppId()).thenReturn("42");

    setConnectorAccount(accountInternal);

    return accountInternal;
  }

  private void defaultConnector() {
    when(accountDatastore.getAccountMaybe()).thenReturn(Maybe.just(currentAccount));

    connector =
        spy(
            new KolibreeConnector(
                backendInteractor,
                synchronizationScheduler,
                accountManager,
                brushingsRepository,
                profileManager,
                profileRepository,
                offlineUpdateDatastore,
                goPirateDatastore,
                accountDatastore,
                currentProfileProvider,
                migrateAccountsFacade,
                gson,
                forceAppUpdater,
                onUserLoggedInCallback));
  }

  private void currentAccountEmptyConnector() {
    when(accountDatastore.getAccountMaybe()).thenReturn(Maybe.empty());

    connector =
        spy(
            new KolibreeConnector(
                backendInteractor,
                synchronizationScheduler,
                accountManager,
                brushingsRepository,
                profileManager,
                profileRepository,
                offlineUpdateDatastore,
                goPirateDatastore,
                accountDatastore,
                currentProfileProvider,
                migrateAccountsFacade,
                gson,
                forceAppUpdater,
                onUserLoggedInCallback));
  }

  private void setConnectorAccount(@Nullable AccountInternal account) {
    Maybe<AccountInternal> accountMaybe;
    if (account == null) {
      accountMaybe = Maybe.empty();
    } else {
      accountMaybe = Maybe.just(account);
    }

    when(accountDatastore.getAccountMaybe()).thenReturn(accountMaybe);
  }

  @Nullable
  AccountInternal getAccount() {
    return accountDatastore.getAccountMaybe().blockingGet();
  }

  private Request prepareDoLoginSuccessScenario() {
    return prepareDoLoginScenario(mock(AccountInternal.class), new Response(200, ""));
  }

  private Request prepareDoLoginScenario(Response response) {
    return prepareDoLoginScenario(mock(AccountInternal.class), response);
  }

  private Request prepareDoLoginSuccessScenario(AccountInternal accountInternal) {
    return prepareDoLoginScenario(accountInternal, new Response(200, ""));
  }

  private Request prepareDoLoginScenario(AccountInternal accountInternal, Response response) {
    Request request = mock(Request.class);
    String token = "token";
    doReturn(token).when(connector).getAccessToken();
    when(backendInteractor.call(request, token)).thenReturn(response);

    when(gson.fromJson(response.getBody(), AccountInternal.class)).thenReturn(accountInternal);

    Timber.d("Donothing for account %s", accountInternal);
    doNothing().when(connector).saveAccount(accountInternal);

    return request;
  }

  /**
   * Mock a failed request response
   *
   * @param errorMessage non null error message {@link String}
   * @param internalErrorCode int internal error code
   * @return mocked error {@link Response}
   */
  @NonNull
  private static Response mockErrorResponse(@NonNull String errorMessage, int internalErrorCode) {
    final Response response = mock(Response.class);
    when(response.getError()).thenReturn(new ApiError(errorMessage, internalErrorCode, null));
    when(response.getHttpCode()).thenReturn(400);
    when(response.succeeded()).thenReturn(false);

    return response;
  }
}

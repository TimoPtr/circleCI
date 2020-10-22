package com.kolibree.android.accountinternal.account;

import static com.kolibree.android.commons.ApiConstants.DATE_FORMATTER;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.kolibree.android.accountinternal.internal.AccountInternal;
import com.kolibree.android.accountinternal.internal.AccountInternalAdapter;
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore;
import com.kolibree.android.accountinternal.profile.persistence.ProfileInternalAdapter;
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal;
import com.kolibree.android.commons.gson.LocalDateTypeAdapter;
import com.kolibree.android.network.api.ApiError;
import com.kolibree.android.test.BaseMockWebServerTest;
import com.kolibree.android.test.SharedTestUtils;
import com.kolibree.retrofit.ParentalConsentTypeAdapter;
import com.kolibree.sdkws.account.AccountApi;
import com.kolibree.sdkws.account.AccountManagerImpl;
import com.kolibree.sdkws.account.models.PhoneWeChatLinked;
import com.kolibree.sdkws.data.model.PhoneNumberData;
import com.kolibree.sdkws.data.request.BetaData;
import com.kolibree.sdkws.data.request.CreateAccountData;
import com.kolibree.sdkws.exception.WeChatAccountNotRecognizedException;
import java.io.IOException;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.threeten.bp.LocalDate;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@SuppressWarnings("KotlinInternalInJava")
@RunWith(AndroidJUnit4.class)
public class AccountManagerImplRetrofitTest extends BaseMockWebServerTest<AccountApi> {

  private Gson gson =
      new GsonBuilder()
          .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
          .registerTypeAdapter(ParentalConsent.class, new ParentalConsentTypeAdapter())
          .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
          .create();

  private Retrofit retrofit;
  private AccountManagerImpl accountManager;

  @NotNull
  @Override
  protected Context context() {
    return InstrumentationRegistry.getInstrumentation().getTargetContext();
  }

  @Override
  protected AccountApi retrofitService() {
    return retrofit.create(retrofitServiceClass());
  }

  @Override
  protected Class<AccountApi> retrofitServiceClass() {
    return AccountApi.class;
  }

  @Before
  public void setup() {
    gson =
        gson.newBuilder()
            .registerTypeAdapter(ProfileInternal.class, new ProfileInternalAdapter(gson))
            .create();

    gson =
        gson.newBuilder()
            .registerTypeAdapter(AccountInternal.class, new AccountInternalAdapter(gson))
            .create();
    retrofit =
        new Retrofit.Builder()
            .baseUrl(mockWebServer.url("/").toString())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build();

    AccountDatastore accountDatastore = mock(AccountDatastore.class);
    accountManager = new AccountManagerImpl(context(), retrofitService(), accountDatastore, gson);
  }

  /*
  This test in the future will be more useful, we'll test the parsing
   */
  @Test
  public void createAccount_success_returnsResponseBodyWithExpectedJson() throws IOException {
    String jsonResponse = SharedTestUtils.getJson("json/account/create_account_auth_password.json");

    MockResponse mockedResponse = new MockResponse().setResponseCode(200).setBody(jsonResponse);

    mockWebServer.enqueue(mockedResponse);

    AccountInternal response =
        accountManager
            .createAnonymousAccount(CreateAccountData.builder().build())
            .test()
            .assertValueCount(1)
            .values()
            .get(0);

    compareAccountInternals(response, gson.fromJson(jsonResponse, AccountInternal.class));
  }

  @Test
  public void updateAccount_success_returnsResponseBodyWithExpectedJson() throws IOException {
    String jsonResponse = SharedTestUtils.getJson("json/account/update_account_v3.json");

    MockResponse mockedResponse = new MockResponse().setResponseCode(200).setBody(jsonResponse);

    mockWebServer.enqueue(mockedResponse);

    AccountInternal response =
        accountManager
            .createAnonymousAccount(CreateAccountData.builder().build())
            .test()
            .assertValueCount(1)
            .values()
            .get(0);

    compareAccountInternals(response, gson.fromJson(jsonResponse, AccountInternal.class));
  }

  @Test
  public void updateBetaAccount_success_returnsResponseBodyWithExpectedJson() throws IOException {
    String jsonResponse = SharedTestUtils.getJson("json/account/update_account_v3.json");

    MockResponse mockedResponse = new MockResponse().setResponseCode(200).setBody(jsonResponse);

    mockWebServer.enqueue(mockedResponse);

    AccountInternal response =
        accountManager
            .updateBetaAccount(42L, new BetaData(true))
            .test()
            .assertValueCount(1)
            .values()
            .get(0);

    compareAccountInternals(response, gson.fromJson(jsonResponse, AccountInternal.class));
  }

  @Test
  public void createAccountBySms_success_returnsResponseBodyWithExpectedJson() throws IOException {
    String jsonResponse = SharedTestUtils.getJson("json/account/update_account_v3.json");

    MockResponse mockedResponse = new MockResponse().setResponseCode(200).setBody(jsonResponse);

    mockWebServer.enqueue(mockedResponse);

    AccountInternal response =
        accountManager
            .createAccountBySms(CreateAccountData.builder().build())
            .test()
            .assertValueCount(1)
            .values()
            .get(0);

    compareAccountInternals(response, gson.fromJson(jsonResponse, AccountInternal.class));
  }

  @Test
  public void loginBySms_success_returnsResponseBodyWithExpectedJson() throws IOException {
    String jsonResponse = SharedTestUtils.getJson("json/account/update_account_v3.json");

    MockResponse mockedResponse = new MockResponse().setResponseCode(200).setBody(jsonResponse);

    mockWebServer.enqueue(mockedResponse);

    PhoneNumberData data = new PhoneNumberData("06-4024-234234", "verif_token", "verif_code");

    AccountInternal response =
        accountManager.loginBySms(data).test().assertValueCount(1).values().get(0);

    compareAccountInternals(response, gson.fromJson(jsonResponse, AccountInternal.class));
  }

  @Test
  public void loginWithWechat_success_returnsResponseBodyWithExpectedJson() throws IOException {
    String jsonResponse = SharedTestUtils.getJson("json/account/update_account_v3.json");

    MockResponse mockedResponse = new MockResponse().setResponseCode(200).setBody(jsonResponse);

    mockWebServer.enqueue(mockedResponse);

    AccountInternal response =
        accountManager
            .legacyLoginWithWechat("wechat_code")
            .test()
            .assertValueCount(1)
            .values()
            .get(0);

    // assertEquals(jsonResponse, gson.toJson(response));
    compareAccountInternals(response, gson.fromJson(jsonResponse, AccountInternal.class));
  }

  @Test
  public void loginByGoogle_success_returnsResponseBodyWithExpectedJson() throws IOException {
    String jsonResponse = SharedTestUtils.getJson("json/account/update_account_v3.json");

    MockResponse mockedResponse = new MockResponse().setResponseCode(200).setBody(jsonResponse);

    mockWebServer.enqueue(mockedResponse);

    CreateAccountData data =
        CreateAccountData.builder()
            .setEmail("paul@atreides.cl")
            .setGoogleId("muad'dib")
            .setGoogleIdToken("kwisatz.haderach")
            .build();

    AccountInternal response =
        accountManager.loginByGoogle(data).test().assertValueCount(1).values().get(0);

    compareAccountInternals(response, gson.fromJson(jsonResponse, AccountInternal.class));
  }

  @Test
  public void registerWithGoogle_success_returnsResponseBodyWithExpectedJson() throws IOException {
    String jsonResponse = SharedTestUtils.getJson("json/account/update_account_v3.json");

    MockResponse mockedResponse = new MockResponse().setResponseCode(200).setBody(jsonResponse);

    mockWebServer.enqueue(mockedResponse);

    CreateAccountData data =
        CreateAccountData.builder()
            .setFirstName("Paul")
            .setEmail("paul@atreides.cl")
            .setCountry("Caladan")
            .setIsBetaAccount(false)
            .setParentalConsentGiven(true)
            .setGoogleId("muad'dib")
            .setGoogleIdToken("kwisatz.haderach")
            .build();

    AccountInternal response =
        accountManager.registerWithGoogle(data).test().assertValueCount(1).values().get(0);

    compareAccountInternals(response, gson.fromJson(jsonResponse, AccountInternal.class));
  }

  @Test
  public void attemptLoginWithWechat_success_returnsResponseBodyWithExpectedJson()
      throws IOException {
    String jsonResponse = SharedTestUtils.getJson("json/account/update_account_v3.json");

    MockResponse mockedResponse = new MockResponse().setResponseCode(200).setBody(jsonResponse);

    mockWebServer.enqueue(mockedResponse);

    AccountInternal response =
        accountManager
            .legacyLoginWithWechat("wechat_code")
            .test()
            .assertValueCount(1)
            .values()
            .get(0);

    // assertEquals(jsonResponse, gson.toJson(response));
    compareAccountInternals(response, gson.fromJson(jsonResponse, AccountInternal.class));
  }

  @Test
  public void attemptLoginWithWechat_success_returnsResponseBodyWithExpectedToken()
      throws IOException {
    String expectedToken = "helloWorld";
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("token", expectedToken);
    String jsonResponse = jsonObject.toString();

    MockResponse mockedResponse = new MockResponse().setResponseCode(202).setBody(jsonResponse);

    mockWebServer.enqueue(mockedResponse);

    accountManager
        .internalAttemptLoginWithWechat("wechat_code")
        .test()
        .assertError(
            error ->
                error instanceof WeChatAccountNotRecognizedException
                    && ((WeChatAccountNotRecognizedException) error)
                        .getLoginAttemptToken()
                        .equals(expectedToken));
  }

  @Test
  public void registerWithWechat_success_returnsResponseBodyWithExpectedJson() throws IOException {
    String jsonResponse = SharedTestUtils.getJson("json/account/update_account_v3.json");

    MockResponse mockedResponse = new MockResponse().setResponseCode(200).setBody(jsonResponse);

    mockWebServer.enqueue(mockedResponse);

    AccountInternal response =
        accountManager
            .registerWithWechat(new JsonObject())
            .test()
            .assertValueCount(1)
            .values()
            .get(0);

    compareAccountInternals(response, gson.fromJson(jsonResponse, AccountInternal.class));
  }

  @Test
  public void createEmailAccount_success_returnsResponseBodyWithExpectedJson() throws IOException {
    String jsonResponse = SharedTestUtils.getJson("json/account/create_account_auth_password.json");

    MockResponse mockedResponse = new MockResponse().setResponseCode(200).setBody(jsonResponse);

    mockWebServer.enqueue(mockedResponse);

    AccountInternal response =
        accountManager
            .createEmailAccount(CreateAccountData.builder().build())
            .test()
            .assertValueCount(1)
            .values()
            .get(0);

    compareAccountInternals(response, gson.fromJson(jsonResponse, AccountInternal.class));
  }

  @Test
  public void checkPhoneNumberAssociation_success_returnsResponseBodyWithExpectedJson()
      throws IOException {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("phone_linked", true);
    jsonObject.addProperty("wechat_linked", true);
    String jsonResponse = jsonObject.toString();

    MockResponse mockedResponse = new MockResponse().setResponseCode(200).setBody(jsonResponse);

    mockWebServer.enqueue(mockedResponse);

    PhoneWeChatLinked response =
        accountManager
            .checkPhoneNumberAssociation("test number", "test token", "test code")
            .test()
            .assertValueCount(1)
            .values()
            .get(0);

    assertEquals(response.getPhoneLinked(), true);
    assertEquals(response.getWechatLinked(), true);
  }

  /*
  LOGOUT
   */

  @Test
  public void logout_response204_returnsCompletedCompletable() {
    MockResponse mockedResponse = new MockResponse().setResponseCode(204);

    mockWebServer.enqueue(mockedResponse);

    accountManager.logout(1, "", "").test().assertNoErrors().assertComplete();
  }

  @Test
  public void logout_responseError401InvalidAccessToken_returnsCompletedCompletable()
      throws IOException {
    String responseBody = SharedTestUtils.getJson("json/invalid_access_token.json");
    MockResponse mockedResponse = new MockResponse().setResponseCode(401).setBody(responseBody);

    mockWebServer.enqueue(mockedResponse);

    accountManager.logout(1, "", "").test().assertNoErrors().assertComplete();
  }

  @Test
  public void logout_responseError401ExpiredToken_returnsCompletableApiError() throws IOException {
    String responseBody = SharedTestUtils.getJson("json/expired_access_token.json");
    MockResponse mockedResponse = new MockResponse().setResponseCode(401).setBody(responseBody);

    mockWebServer.enqueue(mockedResponse);

    accountManager.logout(1, "", "").test().assertError(ApiError.class);
  }

  /*
  Utils : compare just the fields mapped
   */
  private void compareAccountInternals(AccountInternal account, AccountInternal expected) {
    assertEquals(expected.getAccessToken(), account.getAccessToken());
    assertEquals(expected.isEmailVerified(), account.isEmailVerified());
    assertEquals(expected.getParentalConsent(), account.getParentalConsent());
    assertEquals(expected.getFacebookId(), account.getFacebookId());
    assertEquals(expected.getEmail(), account.getEmail());
    assertEquals(expected.isBeta(), account.isBeta());
    assertEquals(expected.getOwnerProfileId(), account.getOwnerProfileId());
    assertEquals(expected.getId(), account.getId());
    assertEquals(expected.getRefreshToken(), account.getRefreshToken());
    assertEquals(expected.getTokenExpires(), account.getTokenExpires());
    assertEquals(expected.isAllowDataCollecting(), account.isAllowDataCollecting());
    assertEquals(expected.isDigestEnabled(), account.isDigestEnabled());
    assertEquals(expected.getPubId(), account.getPubId());
    assertEquals(expected.getAppId(), account.getAppId());
    assertEquals(expected.getPhoneNumber(), account.getPhoneNumber());

    List<ProfileInternal> profiles = account.getInternalProfiles();
    ProfileInternal profile = profiles.get(0);

    List<ProfileInternal> expectedProfiles = expected.getInternalProfiles();
    ProfileInternal expectedProfile = expectedProfiles.get(0);

    assertEquals(expectedProfile.getFirstName(), profile.getFirstName());
    assertEquals(expectedProfile.getPictureUrl(), profile.getPictureUrl());
    assertEquals(expectedProfile.getPictureUploadUrl(), profile.getPictureUploadUrl());
    assertEquals(expectedProfile.isOwnerProfile(), profile.isOwnerProfile());
    assertEquals(expectedProfile.getAddressCountry(), profile.getAddressCountry());
    assertEquals(expectedProfile.getGender(), profile.getGender());
    assertEquals(expectedProfile.getHandedness(), profile.getHandedness());
    assertEquals(
        DATE_FORMATTER.format(expectedProfile.getBirthday()),
        DATE_FORMATTER.format(profile.getBirthday()));
    assertEquals(expectedProfile.getExactBirthday(), profile.getExactBirthday());
    assertEquals(expectedProfile.getAccountId(), profile.getAccountId());
    assertEquals(expectedProfile.getBrushingTime(), profile.getBrushingTime());
    assertEquals(expectedProfile.getId(), profile.getId());
    assertEquals(expectedProfile.getCreationDate(), profile.getCreationDate());

    assertEquals(expectedProfile.getPoints(), profile.getPoints());
  }
}

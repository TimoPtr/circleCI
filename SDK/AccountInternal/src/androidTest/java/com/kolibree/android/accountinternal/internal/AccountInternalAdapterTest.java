package com.kolibree.android.accountinternal.internal;

import static com.kolibree.android.commons.ApiConstants.DATE_FORMATTER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kolibree.android.accountinternal.account.ParentalConsent;
import com.kolibree.android.accountinternal.profile.persistence.ProfileInternalAdapter;
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal;
import com.kolibree.android.commons.gson.LocalDateTypeAdapter;
import com.kolibree.android.test.BaseInstrumentationTest;
import com.kolibree.android.test.SharedTestUtils;
import com.kolibree.retrofit.ParentalConsentTypeAdapter;
import java.io.IOException;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.threeten.bp.LocalDate;

@RunWith(AndroidJUnit4.class)
public class AccountInternalAdapterTest extends BaseInstrumentationTest {

  @NotNull
  @Override
  protected Context context() {
    return InstrumentationRegistry.getInstrumentation().getTargetContext();
  }

  private Gson gson =
      new GsonBuilder()
          .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
          .registerTypeAdapter(ParentalConsent.class, new ParentalConsentTypeAdapter())
          .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
          .create();

  @Override
  public void setUp() throws Exception {
    super.setUp();
    gson =
        gson.newBuilder()
            .registerTypeAdapter(ProfileInternal.class, new ProfileInternalAdapter(gson))
            .create();

    gson =
        gson.newBuilder()
            .registerTypeAdapter(AccountInternal.class, new AccountInternalAdapter(gson))
            .create();
  }

  private static final String CLOSING_BRACKET = "}";

  @Test
  public void parse_consentGivenNotPresentInJSON_returnsConsentGivenUNKNOWN() throws IOException {

    String content = accountResponseWithDefaultEmail() + CLOSING_BRACKET;
    final AccountInternal account = gson.fromJson(content, AccountInternal.class);

    assertEquals(ParentalConsent.UNKNOWN, account.getParentalConsent());
  }

  @Test
  public void parse_parentalConsentNullInJSON_returnsConsentGivenUNKNOWN() throws IOException {
    final AccountInternal account =
        gson.fromJson(fullJsonWithParentalConsent(null), AccountInternal.class);

    assertEquals(ParentalConsent.UNKNOWN, account.getParentalConsent());
  }

  @Test
  public void parse_parentalConsentTrueInJSON_returnsConsentGivenGRANTED() throws IOException {

    final AccountInternal account =
        gson.fromJson(fullJsonWithParentalConsent(true), AccountInternal.class);

    assertEquals(ParentalConsent.GRANTED, account.getParentalConsent());
  }

  @Test
  public void parse_parentalConsentFalseInJSON_returnsConsentGivenPENDING() throws IOException {

    final AccountInternal account =
        gson.fromJson(fullJsonWithParentalConsent(false), AccountInternal.class);

    assertEquals(ParentalConsent.PENDING, account.getParentalConsent());
  }

  @Test
  public void parse_withPhoneNumber() throws IOException {
    String expectedPhoneNumber = "+34123456789";

    String content = accountResponseWithPhoneNumber(expectedPhoneNumber) + CLOSING_BRACKET;
    final AccountInternal account = gson.fromJson(content, AccountInternal.class);

    assertEquals(expectedPhoneNumber, account.getPhoneNumber());
  }

  /*
  WeChat
   */

  @Test
  public void readWeChatData_nullJsonWeChatFieldsAreNull() {
    final AccountInternal account = new AccountInternal();
    account.setWechat(null);
    assertNull(account.getWcOpenId());
    assertNull(account.getWcUnionId());
    assertNull(account.getWcAccessToken());
    assertNull(account.getWcRefreshToken());
    assertNull(account.getWcExpiresIn());
    assertNull(account.getWcScope());
  }

  @Test
  public void jsonConstructor_withoutWeChatData_doesNotTryToParseIt() throws IOException {
    final AccountInternal account =
        gson.fromJson(loadJson("account_without_wechat"), AccountInternal.class);
    assertNull(account.getWcOpenId());
    assertNull(account.getWcUnionId());
    assertNull(account.getWcAccessToken());
    assertNull(account.getWcRefreshToken());
    assertNull(account.getWcExpiresIn());
    assertNull(account.getWcScope());
  }

  @Test
  public void jsonConstructor_withWeChatData_parsesInnerData() throws IOException {
    final AccountInternal account =
        gson.fromJson(loadJson("account_with_wechat"), AccountInternal.class);
    assertNotNull(account.getWcOpenId());
    assertNotNull(account.getWcUnionId());
    assertNotNull(account.getWcAccessToken());
    assertNotNull(account.getWcRefreshToken());
    assertNotNull(account.getWcExpiresIn());
    assertNotNull(account.getWcScope());
  }

  @Test
  public void jsonConstructor_verify_all_data_are_correct() throws IOException, JSONException {

    String json = loadJson("account_response_full");

    final JSONObject expected = new JSONObject(json);

    final AccountInternal account = gson.fromJson(json, AccountInternal.class);
    // final AccountInternal account = new AccountInternal(json);

    assertEquals(expected.getString("access_token"), account.getAccessToken());
    assertEquals(expected.getBoolean("email_verified"), account.isEmailVerified());
    assertEquals(ParentalConsent.GRANTED, account.getParentalConsent());
    assertEquals(expected.getString("facebook_id"), account.getFacebookId());
    assertEquals(expected.getString("email"), account.getEmail());
    assertEquals(expected.getBoolean("beta"), account.isBeta());
    assertEquals(expected.getLong("owner_profile_id"), account.getOwnerProfileId());
    assertEquals(expected.getLong("id"), account.getId());
    assertEquals(expected.getString("refresh_token"), account.getRefreshToken());
    assertEquals(expected.getString("token_expires"), account.getTokenExpires());
    assertTrue(account.isAllowDataCollecting());
    assertEquals(expected.getBoolean("weekly_digest_subscription"), account.isDigestEnabled());
    assertEquals(expected.getString("pubid"), account.getPubId());
    assertEquals(expected.getString("appid"), account.getAppId());
    assertEquals(expected.getString("phone_number"), account.getPhoneNumber());

    List<ProfileInternal> profiles = account.getInternalProfiles();
    assertEquals(expected.getJSONArray("profiles").length(), profiles.size());
    ProfileInternal profile = profiles.get(0);
    JSONObject jsonProfile = expected.getJSONArray("profiles").getJSONObject(0);

    assertEquals(jsonProfile.getString("first_name"), profile.getFirstName());
    assertEquals(jsonProfile.getString("picture"), profile.getPictureUrl());
    assertEquals(jsonProfile.getString("picture_upload_url"), profile.getPictureUploadUrl());
    assertEquals(jsonProfile.getBoolean("is_owner_profile"), profile.isOwnerProfile());
    assertEquals(jsonProfile.getString("address_country"), profile.getAddressCountry());
    assertEquals(jsonProfile.getString("gender"), profile.getGender());
    assertEquals(jsonProfile.getString("survey_handedness"), profile.getHandedness());
    assertEquals(jsonProfile.getString("birthday"), DATE_FORMATTER.format(profile.getBirthday()));
    assertEquals(jsonProfile.getBoolean("exact_birthday"), profile.getExactBirthday());
    assertEquals(jsonProfile.getLong("account"), profile.getAccountId());
    assertEquals(jsonProfile.getLong("brushing_goal_time"), profile.getBrushingTime());
    assertEquals(jsonProfile.getLong("id"), profile.getId());
    assertEquals(jsonProfile.getString("created_at"), profile.getCreationDate());

    JSONObject stat = jsonProfile.getJSONObject("stats");

    assertEquals(stat.getLong("points"), profile.getPoints());
  }

  @Test
  public void jsonConstructor_verify_all_data_are_correct_with_empty_value()
      throws IOException, JSONException {

    String json = loadJson("account_response_with_empty_value");

    final JSONObject expected = new JSONObject(json);

    final AccountInternal account = gson.fromJson(json, AccountInternal.class);
    // final AccountInternal account = new AccountInternal(json);

    assertEquals("", account.getAccessToken());
    assertFalse(account.isEmailVerified());
    assertEquals(ParentalConsent.GRANTED, account.getParentalConsent());
    assertNull(account.getFacebookId());
    assertNull(account.getEmail());
    assertFalse(account.isBeta());
    assertEquals(expected.getLong("owner_profile_id"), account.getOwnerProfileId());
    assertEquals(expected.getLong("id"), account.getId());
    assertEquals("", account.getRefreshToken());
    assertEquals("", account.getTokenExpires());
    assertTrue(account.isAllowDataCollecting());
    assertEquals(expected.getBoolean("weekly_digest_subscription"), account.isDigestEnabled());
    assertEquals(expected.getString("pubid"), account.getPubId());
    assertNull(account.getAppId());
    assertNull(account.getPhoneNumber());

    List<ProfileInternal> profiles = account.getInternalProfiles();
    assertEquals(expected.getJSONArray("profiles").length(), profiles.size());
    ProfileInternal profile = profiles.get(0);
    JSONObject jsonProfile = expected.getJSONArray("profiles").getJSONObject(0);

    assertEquals(jsonProfile.getString("first_name"), profile.getFirstName());
    assertEquals(jsonProfile.getString("picture"), profile.getPictureUrl());
    assertEquals(jsonProfile.getString("picture_upload_url"), profile.getPictureUploadUrl());
    assertEquals(jsonProfile.getBoolean("is_owner_profile"), profile.isOwnerProfile());
    assertEquals(jsonProfile.getString("address_country"), profile.getAddressCountry());
    assertEquals(jsonProfile.getString("gender"), profile.getGender());
    assertEquals(jsonProfile.getString("survey_handedness"), profile.getHandedness());
    assertEquals(jsonProfile.getString("birthday"), DATE_FORMATTER.format(profile.getBirthday()));
    assertEquals(jsonProfile.getBoolean("exact_birthday"), profile.getExactBirthday());
    assertEquals(jsonProfile.getLong("account"), profile.getAccountId());
    assertEquals(jsonProfile.getLong("brushing_goal_time"), profile.getBrushingTime());
    assertEquals(jsonProfile.getLong("id"), profile.getId());
    assertEquals(jsonProfile.getString("created_at"), profile.getCreationDate());

    JSONObject stat = jsonProfile.getJSONObject("stats");

    assertEquals(stat.getLong("points"), profile.getPoints());
  }

  /*
  UTILS
   */

  @Test
  public void parse_nullEmail() throws IOException {
    final AccountInternal account =
        gson.fromJson(loadJson("account_response_phone_number_null_email"), AccountInternal.class);
    assertNull(account.getEmail());
    assertNull(account.getFacebookId());
  }

  @NonNull
  private String accountResponseWithDefaultEmail() throws IOException {
    return String.format(responseWithoutClosingBracket(), "nicolas@kolibree.com", null);
  }

  @NonNull
  private String accountResponseWithPhoneNumber(@NonNull String phoneNumber) throws IOException {
    String string = responseWithoutClosingBracket();
    return String.format(string, null, phoneNumber);
  }

  @NonNull
  private String fullJsonWithParentalConsent(Boolean parentalConsent) throws IOException {
    return accountResponseWithDefaultEmail()
        + ","
        + "\"parental_consent\": "
        + (parentalConsent == null ? "null" : parentalConsent)
        + CLOSING_BRACKET;
  }

  private String responseWithoutClosingBracket() throws IOException {
    return loadJson("account_response_without_closing_bracket");
  }

  private String loadJson(String fileName) throws IOException {
    return SharedTestUtils.getJson("json/account/" + fileName + ".json");
  }
}

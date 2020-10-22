package com.kolibree.sdkws.data.model;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.kolibree.sdkws.data.JSONModel;
import java.util.Objects;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.Duration;

/** Created by aurelien on 27/07/15. */
@Keep
public class LoginData implements JSONModel {

  private static final String FIELD_EMAIL = "email";
  private static final String FIELD_PASSWORD = "password";

  private static final String FIELD_TEST = "test";
  private static final String FIELD_DURATION = "duration";

  private final String email;
  private final String password;

  /*
  Tweak these fields to set the access token duration
   */
  @SuppressWarnings("FieldCanBeLocal")
  private final boolean test = false;

  private final Duration accessTokenDuration = null;

  public LoginData(String email, String password) {
    this.email = email;
    this.password = password;
  }

  @NonNull
  @Override
  public String toJsonString() throws JSONException {
    final JSONObject json = new JSONObject();
    json.put(FIELD_EMAIL, email);
    json.put(FIELD_PASSWORD, password);

    //noinspection ConstantConditions
    if (accessTokenDuration != null) {
      json.put(FIELD_TEST, test);
      json.put(FIELD_DURATION, accessTokenDuration.getSeconds());
    }

    return json.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LoginData loginData = (LoginData) o;
    return Objects.equals(email, loginData.email) && Objects.equals(password, loginData.password);
  }

  @Override
  public int hashCode() {

    return Objects.hash(email, password);
  }
}

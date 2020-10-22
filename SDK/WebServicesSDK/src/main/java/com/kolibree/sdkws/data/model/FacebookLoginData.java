package com.kolibree.sdkws.data.model;

import static com.kolibree.sdkws.Constants.FIELD_EMAIL;
import static com.kolibree.sdkws.Constants.FIELD_FACEBOOK_AUTH_TOKEN;
import static com.kolibree.sdkws.Constants.FIELD_FACEBOOK_ID;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.kolibree.sdkws.data.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

/** Created by aurelien on 27/07/15. */
@Keep
public class FacebookLoginData implements JSONModel {

  private static final String FIELD_FIRST_NAME = "first_name";
  private static final String FIELD_LAST_NAME = "last_name";
  private static final String FIELD_GENDER = "gender";
  private static final String FIELD_BIRTHDAY = "birthday";
  private static final String FIELD_COUNTRY = "country";

  private String facebookId;
  private String facebookAuthToken;

  private String firstName;
  private String lastName;
  private String gender;
  private String birthday;
  private String country;
  private String email;

  public FacebookLoginData(String facebookId, String facebookAuthToken) {
    this.facebookId = facebookId;
    this.facebookAuthToken = facebookAuthToken;
  }

  public FacebookLoginData(
      String facebookId,
      String facebookAuthToken,
      String email,
      String firstName,
      String lastName,
      String gender,
      String birthday,
      String country) {
    this.facebookId = facebookId;
    this.facebookAuthToken = facebookAuthToken;
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.gender = gender;
    this.birthday = birthday;
    this.country = country;
  }

  @NonNull
  @Override
  public String toJsonString() throws JSONException {
    final JSONObject json = new JSONObject();
    json.put(FIELD_FACEBOOK_ID, facebookId);
    json.put(FIELD_FACEBOOK_AUTH_TOKEN, facebookAuthToken);

    if (firstName != null) {
      json.put(FIELD_FIRST_NAME, firstName);
      json.put(FIELD_LAST_NAME, lastName);
      json.put(FIELD_BIRTHDAY, birthday);
      json.put(FIELD_COUNTRY, country);
      json.put(FIELD_GENDER, gender);
      json.put(FIELD_EMAIL, email);
      json.put(FIELD_EMAIL, email);
    }

    return json.toString();
  }
}

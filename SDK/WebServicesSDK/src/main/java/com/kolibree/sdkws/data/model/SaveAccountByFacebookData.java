package com.kolibree.sdkws.data.model;

import static com.kolibree.sdkws.Constants.FIELD_APP_ID;
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
public class SaveAccountByFacebookData implements JSONModel {

  private String facebookId;
  private String facebookAuthToken;
  private String email;
  private String appId;

  public SaveAccountByFacebookData(
      String facebookId, String facebookAuthToken, String email, String appId) {
    this.facebookId = facebookId;
    this.facebookAuthToken = facebookAuthToken;
    this.email = email;
    this.appId = appId;
  }

  @NonNull
  @Override
  public String toJsonString() throws JSONException {
    final JSONObject json = new JSONObject();

    json.put(FIELD_FACEBOOK_ID, facebookId);
    json.put(FIELD_FACEBOOK_AUTH_TOKEN, facebookAuthToken);
    json.put(FIELD_EMAIL, email);
    json.put(FIELD_APP_ID, appId);

    return json.toString();
  }
}

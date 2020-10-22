package com.kolibree.sdkws.data.model;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.kolibree.sdkws.data.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

/** Created by aurelien on 17/09/15. */
@Keep
public class EmailData implements JSONModel {
  private static final String FIELD_EMAIL = "email";

  private String email;

  public EmailData(String email) {
    this.email = email;
  }

  @NonNull
  @Override
  public String toJsonString() throws JSONException {
    final JSONObject json = new JSONObject();
    json.put(FIELD_EMAIL, email);
    return json.toString();
  }
}

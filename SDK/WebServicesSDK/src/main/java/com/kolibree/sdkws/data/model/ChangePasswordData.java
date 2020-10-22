package com.kolibree.sdkws.data.model;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.kolibree.sdkws.data.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

/** Created by aurelien on 17/09/15. */
@Keep
public class ChangePasswordData implements JSONModel {
  private static final String FIELD_OLD_PASSWORD = "old_password";
  private static final String FIELD_NEW_PASSWORD = "new_password";

  private String oldPassword;
  private String newPassword;

  public ChangePasswordData(String oldPassword, String newPassword) {
    this.oldPassword = oldPassword;
    this.newPassword = newPassword;
  }

  @NonNull
  @Override
  public String toJsonString() throws JSONException {
    final JSONObject json = new JSONObject();
    json.put(FIELD_OLD_PASSWORD, oldPassword);
    json.put(FIELD_NEW_PASSWORD, newPassword);

    return json.toString();
  }
}

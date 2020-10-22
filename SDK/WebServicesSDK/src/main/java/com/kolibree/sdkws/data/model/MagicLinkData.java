package com.kolibree.sdkws.data.model;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kolibree.sdkws.data.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.Duration;

/** Magic link code */
@Keep
public class MagicLinkData implements JSONModel {

  private static final String FIELD_CODE = "code";

  private static final String FIELD_TEST = "test";
  private static final String FIELD_DURATION = "duration";

  @JsonProperty(FIELD_CODE)
  private String code;

  /*
  Tweak these fields to set the access token duration
   */
  @SuppressWarnings("FieldCanBeLocal")
  private final boolean test = false;

  private final Duration accessTokenDuration = null;

  public MagicLinkData(@NonNull String code) {
    this.code = code;
  }

  public MagicLinkData() {}

  @NonNull
  @Override
  public String toJsonString() throws JSONException {
    final JSONObject json = new JSONObject();
    json.put(FIELD_CODE, code);

    //noinspection ConstantConditions
    if (accessTokenDuration != null) {
      json.put(FIELD_TEST, test);
      json.put(FIELD_DURATION, accessTokenDuration.getSeconds());
    }

    return json.toString();
  }

  @NonNull
  public String getCode() {
    return code;
  }
}

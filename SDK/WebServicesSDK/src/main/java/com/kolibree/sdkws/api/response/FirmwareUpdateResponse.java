package com.kolibree.sdkws.api.response;

import org.json.JSONException;
import org.json.JSONObject;

/** Created by aurelien on 06/01/16. */
public final class FirmwareUpdateResponse {
  private static final String FIELD_LINK = "link";
  private static final String FIELD_FW = "fw";

  private String link;
  private String firmwareVersion;

  public FirmwareUpdateResponse(String raw) throws JSONException {
    final JSONObject json = new JSONObject(raw);
    link = json.getString(FIELD_LINK);
    firmwareVersion = json.getString(FIELD_FW);
  }

  public String getLink() {
    return link;
  }

  public String getFirmwareVersion() {
    return firmwareVersion;
  }
}

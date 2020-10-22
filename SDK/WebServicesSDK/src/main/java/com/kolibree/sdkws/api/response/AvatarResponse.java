package com.kolibree.sdkws.api.response;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;

/** Created by aurelien on 13/10/15. */
public final class AvatarResponse {
  private ArrayList<String> pictures;

  public AvatarResponse(String raw) throws JSONException {
    final JSONArray json = new JSONArray(raw);

    pictures = new ArrayList<>();

    for (int i = 0; i < json.length(); i++) {
      pictures.add(json.getString(i));
    }
  }

  public ArrayList<String> getList() {
    return pictures;
  }
}

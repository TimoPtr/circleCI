package com.kolibree.sdkws.data;

import androidx.annotation.NonNull;
import org.json.JSONException;

/** Created by aurelien on 15/09/15. */
public interface JSONModel {
  @NonNull
  String toJsonString() throws JSONException;
}

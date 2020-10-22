package com.kolibree.sdkws.api.request;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import com.kolibree.sdkws.Constants;
import com.kolibree.sdkws.data.model.UpdateToothbrushData;
import com.kolibree.sdkws.networking.RequestMethod;
import java.util.Locale;
import org.json.JSONException;
import timber.log.Timber;

/** Created by aurelien on 07/03/16. */
public final class UpdateToothbrushRequest extends Request {
  @SuppressLint("StringFormatInTimber")
  public UpdateToothbrushRequest(@NonNull UpdateToothbrushData data, long accountId) {
    super(
        RequestMethod.PATCH,
        String.format(Locale.getDefault(), Constants.SERVICE_UPDATE_TOOTHBRUSH, accountId));
    Timber.d(
        "URL : %s",
        String.format(Locale.getDefault(), Constants.SERVICE_UPDATE_TOOTHBRUSH, accountId));
    try {
      Timber.d("DATA : %s", data.toJsonString());
    } catch (JSONException e) {
      e.printStackTrace();
    }
    setData(data);
  }
}

package com.kolibree.sdkws.api.request;

import androidx.annotation.NonNull;
import com.kolibree.sdkws.Constants;
import com.kolibree.sdkws.networking.RequestMethod;
import java.util.Locale;

/** Update GRU data request */
public class CheckGRUDataRequest extends Request {

  public CheckGRUDataRequest(
      @NonNull String toothbrushModel,
      @NonNull String firmwareVersion,
      @NonNull String hardwareVersion,
      @NonNull String gruDataVersion) {
    super(
        RequestMethod.GET,
        String.format(
            Locale.US,
            Constants.SERVICE_CHECK_GRU_UPDATE,
            toothbrushModel,
            firmwareVersion,
            hardwareVersion,
            gruDataVersion));
  }
}

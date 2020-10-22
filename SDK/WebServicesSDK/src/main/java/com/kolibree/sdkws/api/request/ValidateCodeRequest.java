package com.kolibree.sdkws.api.request;

import androidx.annotation.NonNull;
import com.kolibree.sdkws.Constants;
import com.kolibree.sdkws.data.model.MagicLinkData;
import com.kolibree.sdkws.networking.RequestMethod;

/** Magic link code validation request */
public final class ValidateCodeRequest extends Request {

  public ValidateCodeRequest(@NonNull String code) {
    super(RequestMethod.POST, Constants.SERVICE_VALIDATE_CODE);
    setData(new MagicLinkData(code));
  }
}

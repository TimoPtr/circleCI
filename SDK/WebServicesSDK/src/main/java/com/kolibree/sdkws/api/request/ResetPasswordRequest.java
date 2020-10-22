package com.kolibree.sdkws.api.request;

import com.kolibree.sdkws.Constants;
import com.kolibree.sdkws.data.model.EmailData;
import com.kolibree.sdkws.networking.RequestMethod;

/** Created by aurelien on 17/09/15. */
public class ResetPasswordRequest extends Request {
  public ResetPasswordRequest(EmailData data) {
    super(RequestMethod.PUT, Constants.SERVICE_RESET_PASSWORD);
    setData(data);
  }
}

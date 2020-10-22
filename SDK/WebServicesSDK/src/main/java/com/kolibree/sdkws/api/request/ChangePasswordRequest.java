package com.kolibree.sdkws.api.request;

import com.kolibree.sdkws.Constants;
import com.kolibree.sdkws.data.model.ChangePasswordData;
import com.kolibree.sdkws.networking.RequestMethod;

/** Created by aurelien on 17/09/15. */
public class ChangePasswordRequest extends Request {
  public ChangePasswordRequest(long accountId, ChangePasswordData data) {
    super(RequestMethod.PUT, String.format(Constants.SERVICE_CHANGE_PASSWORD, accountId));
    setData(data);
  }
}

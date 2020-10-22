package com.kolibree.sdkws.api.request;

import static com.kolibree.android.network.NetworkConstants.SERVICE_REQUEST_TOKEN_V2;

import com.kolibree.sdkws.data.JSONModel;
import com.kolibree.sdkws.networking.RequestMethod;

/** Created by aurelien on 27/07/15. */
public final class LoginRequest extends Request {
  public LoginRequest(JSONModel loginData) {
    super(RequestMethod.POST, SERVICE_REQUEST_TOKEN_V2);
    setData(loginData);
  }
}

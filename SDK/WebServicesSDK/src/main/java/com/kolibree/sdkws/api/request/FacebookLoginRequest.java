package com.kolibree.sdkws.api.request;

import com.kolibree.sdkws.Constants;
import com.kolibree.sdkws.data.model.FacebookLoginData;
import com.kolibree.sdkws.networking.RequestMethod;

/** Created by aurelien on 15/10/15. */
public class FacebookLoginRequest extends Request {
  public FacebookLoginRequest(FacebookLoginData data) {
    super(RequestMethod.POST, Constants.SERVICE_REQUEST_TOKEN_FACEBOOK);
    setData(data);
  }
}

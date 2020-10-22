package com.kolibree.sdkws.api.request;

import com.kolibree.sdkws.Constants;
import com.kolibree.sdkws.data.model.SaveAccountByFacebookData;
import com.kolibree.sdkws.networking.RequestMethod;

/** Created by aurelien on 15/10/15. */
public class SaveAccountByFacebookRequest extends Request {

  public SaveAccountByFacebookRequest(SaveAccountByFacebookData data) {
    super(RequestMethod.POST, Constants.SERVICE_REQUEST_TOKEN_FACEBOOK);
    setData(data);
  }
}

package com.kolibree.sdkws.api.request;

import com.kolibree.sdkws.Constants;
import com.kolibree.sdkws.networking.RequestMethod;

/** Created by aurelien on 13/10/15. */
public final class GetDefaultAvatarRequest extends Request {
  public GetDefaultAvatarRequest() {
    super(RequestMethod.GET, Constants.SERVICE_GET_DEFAULT_AVATARS);
  }
}

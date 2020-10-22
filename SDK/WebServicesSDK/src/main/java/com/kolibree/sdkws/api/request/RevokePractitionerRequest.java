package com.kolibree.sdkws.api.request;

import com.kolibree.sdkws.Constants;
import com.kolibree.sdkws.networking.RequestMethod;

/** Created by aurelien on 23/12/15. */
public final class RevokePractitionerRequest extends Request {
  public RevokePractitionerRequest(String token) {
    super(RequestMethod.DELETE, String.format(Constants.SERVICE_REVOKE_PRACTITIONERS, token));
  }
}

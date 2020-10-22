package com.kolibree.sdkws.api.request;

import com.kolibree.sdkws.Constants;
import com.kolibree.sdkws.networking.RequestMethod;

/** Created by aurelien on 23/12/15. */
public final class GetPractitionerRequest extends Request {
  public GetPractitionerRequest(long accountId, long profileId) {
    super(
        RequestMethod.GET,
        String.format(Constants.SERVICE_GET_PRACTITIONERS, accountId, profileId));
  }
}

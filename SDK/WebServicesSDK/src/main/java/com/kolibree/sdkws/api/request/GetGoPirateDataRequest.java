package com.kolibree.sdkws.api.request;

import com.kolibree.sdkws.Constants;
import com.kolibree.sdkws.networking.RequestMethod;

/** Created by aurelien on 16/11/15. */
public final class GetGoPirateDataRequest extends Request {
  public GetGoPirateDataRequest(long accountId, long profileId) {
    super(
        RequestMethod.GET,
        String.format(Constants.SERVICE_GET_GO_PIRATE_DATA, accountId, profileId));
  }
}

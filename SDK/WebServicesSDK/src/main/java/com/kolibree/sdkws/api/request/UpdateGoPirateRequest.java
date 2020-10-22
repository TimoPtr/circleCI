package com.kolibree.sdkws.api.request;

import com.kolibree.sdkws.Constants;
import com.kolibree.sdkws.data.model.gopirate.UpdateGoPirateData;
import com.kolibree.sdkws.networking.RequestMethod;

/** Created by aurelien on 16/11/15. */
public final class UpdateGoPirateRequest extends Request {
  public UpdateGoPirateRequest(long accountId, long profileId, UpdateGoPirateData data) {
    super(
        data == null || !data.hasBrushing() ? RequestMethod.PUT : RequestMethod.POST,
        String.format(Constants.SERVICE_UPDATE_GO_PIRATE_DATA, accountId, profileId));

    if (data != null) {
      setData(data);
    }
  }
}
